package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Order;
import ar.edu.itba.paw.model.OrderItem;
import ar.edu.itba.paw.model.OrderType;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.util.PaginatedResult;
import ar.edu.itba.paw.persistance.OrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class OrderJdbcDao implements OrderDao {

    private static final String SelectFullBase = "SELECT " + TableFields.ORDERS_FIELDS + ", " + TableFields.RESTAURANTS_FIELDS + ", " + TableFields.USERS_FIELDS + ", " + TableFields.ORDER_ITEMS_FIELDS + ", " + TableFields.PRODUCTS_FIELDS + ", " + TableFields.CATEGORIES_FIELDS + " FROM orders JOIN restaurants ON orders.restaurant_id = restaurants.restaurant_id JOIN users on orders.user_id = users.user_id LEFT OUTER JOIN order_items ON orders.order_id = order_items.order_id LEFT OUTER JOIN products ON order_items.product_id = products.product_id LEFT OUTER JOIN categories ON products.category_id = categories.category_id";
    private static final String SelectFullEndOrderById = " ORDER BY orders.order_id, order_items.line_number";

    private static final String SelectOrdersOnlyBase = "SELECT " + TableFields.ORDERS_FIELDS + " FROM orders";
    private static final String IsPendingCond = "date_confirmed IS NULL AND date_cancelled IS NULL";
    private static final String IsConfirmedCond = "date_confirmed IS NOT NULL AND date_ready IS NULL AND date_cancelled IS NULL";
    private static final String IsReadyCond = "date_ready IS NOT NULL AND date_delivered IS NULL AND date_cancelled IS NULL";
    private static final String IsDeliveredCond = "date_delivered IS NOT NULL";
    private static final String IsCancelledCond = "date_cancelled IS NOT NULL";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsertOrder;
    private final SimpleJdbcInsert jdbcInsertOrderItem;


    @Autowired
    public OrderJdbcDao(final DataSource ds) {
        jdbcTemplate = new JdbcTemplate(ds);
        jdbcInsertOrder = new SimpleJdbcInsert(ds)
                .withTableName("orders")
                .usingColumns("order_type", "restaurant_id", "user_id", "table_number", "address")
                .usingGeneratedKeyColumns("order_id");
        jdbcInsertOrderItem = new SimpleJdbcInsert(ds)
                .withTableName("order_items")
                .usingColumns("order_id", "product_id", "line_number", "quantity", "comment");
    }

    private Order create(OrderType orderType, int restaurantId, int userId, String address, Integer tableNumber, List<OrderItem> items) {
        final Map<String, Object> orderData = new HashMap<>();
        orderData.put("order_type", orderType.ordinal());
        orderData.put("restaurant_id", restaurantId);
        orderData.put("user_id", userId);

        if (address != null) {
            orderData.put("address", address);
        }

        if (tableNumber != null) {
            orderData.put("tableNumber", tableNumber);
        }

        final int orderId = jdbcInsertOrder.executeAndReturnKey(orderData).intValue();

        insertItems(items, orderId);

        return getById(orderId).get();
    }

    private void insertItems(List<OrderItem> items, int orderId) {
        final Map<String, Object> orderItemData = new HashMap<>();
        for (OrderItem item : items) {
            orderItemData.clear();
            orderItemData.put("order_id", orderId);
            orderItemData.put("product_id", item.getProduct().getProductId());
            orderItemData.put("line_number", item.getLineNumber());
            orderItemData.put("quantity", item.getQuantity());
            orderItemData.put("comment", item.getComment());
            jdbcInsertOrderItem.execute(orderItemData);
        }
    }

    @Override
    public Optional<Order> getById(int orderId) {
        return jdbcTemplate.query(
                SelectFullBase + " WHERE orders.order_id = ?" + SelectFullEndOrderById,
                Extractors.ORDER_EXTRACTOR,
                orderId
        ).stream().findFirst();
    }

    @Override
    public PaginatedResult<Order> getByUser(int userId, int pageNumber, int pageSize) {
        int pageIdx = pageNumber - 1;
        List<Order> results = jdbcTemplate.query(
                "WITH orders AS (SELECT * FROM orders LIMIT ? OFFSET ?) " + SelectFullBase + " WHERE orders.user_id = ?" + SelectFullEndOrderById,
                Extractors.ORDER_EXTRACTOR,
                pageSize,
                pageIdx * pageSize,
                userId
        );

        int count = jdbcTemplate.query(
                "SELECT COUNT(*) AS c FROM orders WHERE orders.user_id = ?",
                SimpleRowMappers.COUNT_ROW_MAPPER,
                userId
        ).get(0);

        return new PaginatedResult<>(results, pageNumber, pageSize, count);
    }

    @Override
    public PaginatedResult<Order> getByRestaurant(int restaurantId, int pageNumber, int pageSize) {
        int pageIdx = pageNumber - 1;
        List<Order> results = jdbcTemplate.query(
                "WITH orders AS (SELECT * FROM orders LIMIT ? OFFSET ?) " + SelectFullBase + " WHERE orders.restaurant_id = ?" + SelectFullEndOrderById,
                Extractors.ORDER_EXTRACTOR,
                pageSize,
                pageIdx * pageSize,
                restaurantId
        );

        int count = jdbcTemplate.query(
                "SELECT COUNT(*) AS c FROM orders WHERE orders.restaurant_id = ?",
                SimpleRowMappers.COUNT_ROW_MAPPER,
                restaurantId
        ).get(0);

        return new PaginatedResult<>(results, pageNumber, pageSize, count);
    }

    @Override
    public boolean markAsConfirmed(int orderId) {
        return jdbcTemplate.update(
                "UPDATE orders SET date_confirmed = now() WHERE order_id = ? AND " + IsPendingCond,
                orderId
        ) > 0;
    }

    @Override
    public boolean markAsReady(int orderId) {
        return jdbcTemplate.update(
                "UPDATE orders SET date_ready = now() WHERE order_id = ? AND " + IsConfirmedCond,
                orderId
        ) > 0;
    }

    @Override
    public boolean markAsDelivered(int orderId) {
        return jdbcTemplate.update(
                "UPDATE orders SET date_delivered = now() WHERE order_id = ? AND " + IsReadyCond,
                orderId
        ) > 0;
    }

    @Override
    public boolean markAsCancelled(int orderId) {
        return jdbcTemplate.update(
                "UPDATE orders SET date_cancelled = now() WHERE order_id = ? AND NOT(" + IsCancelledCond + ") AND NOT(" + IsDeliveredCond + ")",
                orderId
        ) > 0;
    }

    @Override
    public boolean updateAddress(int orderId, String address) {
        return jdbcTemplate.update(
                "UPDATE orders SET address = ? WHERE order_id = ? AND order_type = " + OrderType.DELIVERY.ordinal(),
                address,
                orderId
        ) > 0;
    }

    @Override
    public boolean updateTableNumber(int orderId, int tableNumber) {
        return jdbcTemplate.update(
                "UPDATE orders SET table_number = ? WHERE order_id = ? AND order_type = " + OrderType.DINE_IN.ordinal(),
                tableNumber,
                orderId
        ) > 0;
    }

    @Override
    public boolean delete(int orderId) {
        return jdbcTemplate.update("DELETE FROM orders WHERE order_id = ?", orderId) > 0;
    }

    @Override
    public Order createDineIn(int restaurantId, int userId, int tableNumber, List<OrderItem> items) {
        return this.create(OrderType.DINE_IN, restaurantId, userId, null, tableNumber, items);
    }

    @Override
    public Order createTakeaway(int restaurantId, int userId, List<OrderItem> items) {
        return this.create(OrderType.TAKEAWAY, restaurantId, userId, null, null, items);
    }


    @Override
    public Order createDelivery(int restaurantId, int userId, String address, List<OrderItem> items) {
        return this.create(OrderType.DELIVERY, restaurantId, userId, address, null, items);
    }

    @Override
    public OrderItem createOrderItem(Product product, int lineNumber, int quantity, String comment) {
        return new OrderItem(product, lineNumber, quantity, comment);
    }
}
