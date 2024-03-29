package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.exception.ProductNotFoundException;
import ar.edu.itba.paw.model.*;
import ar.edu.itba.paw.persistance.OrderDao;
import ar.edu.itba.paw.util.PaginatedResult;
import ar.edu.itba.paw.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderJpaDao implements OrderDao {

    private final static Logger LOGGER = LoggerFactory.getLogger(OrderJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    private Order create(OrderType orderType, long restaurantId, long userId, String address, Integer tableNumber) {
        final Restaurant restaurant = em.getReference(Restaurant.class, restaurantId);
        final User user = em.getReference(User.class, userId);
        final Order order = new Order(orderType, restaurant, user, address, tableNumber);
        em.persist(order);
        LOGGER.info("Created order id {} type {} for restaurant {} by user {}", order.getOrderId(), orderType, restaurantId, userId);
        return order;
    }

    @Override
    public Order createDelivery(long restaurantId, long userId, String address) {
        return this.create(OrderType.DELIVERY, restaurantId, userId, address, null);
    }

    @Override
    public Order createTakeaway(long restaurantId, long userId) {
        return this.create(OrderType.TAKEAWAY, restaurantId, userId, null, null);
    }

    @Override
    public Order createDineIn(long restaurantId, long userId, int tableNumber) {
        return this.create(OrderType.DINE_IN, restaurantId, userId, null, tableNumber);
    }

    @Override
    public OrderItem createOrderItem(long restaurantId, long productId, int lineNumber, int quantity, String comment) {
        final Product product = em.find(Product.class, productId);
        if (product == null)
            throw new ProductNotFoundException();
        if (product.getDeleted() || !product.getAvailable())
            throw new IllegalStateException("exception.IllegalStateException.createOrderItem.productActiveOrDeleted");
        final Category category = em.find(Category.class, product.getCategoryId());
        if (category.getRestaurantId() != restaurantId)
            throw new IllegalStateException("exception.IllegalStateException.createOrderItem.productDoesNotBelongToRestaurant");

        return new OrderItem(product, lineNumber, quantity, comment);
    }

    @Override
    public Optional<Order> getById(long orderId) {
        return Optional.ofNullable(em.find(Order.class, orderId));
    }

    @Override
    public Optional<List<OrderItem>> getOrderItemsById(long orderId) {
        final TypedQuery<OrderItem> query = em.createQuery(
                "FROM OrderItem WHERE orderId = :orderId ORDER BY lineNumber",
                OrderItem.class
        );
        query.setParameter("orderId", orderId);

        // Note: orders must always have at least one item, so if we don't get any items, the order doesn't exist.
        final List<OrderItem> items = query.getResultList();
        return items.isEmpty() ? Optional.empty() : Optional.of(items);
    }

    private static String getInProgressConditionString(boolean onlyInProgress) {
        return onlyInProgress ? "date_delivered IS NULL AND date_cancelled IS NULL" : null;
    }

    private static String getOrderStatusConditionString(OrderStatus orderStatus) {
        if (orderStatus == null)
            return null;

        switch (orderStatus) {
            case PENDING:
                return "(date_confirmed IS NULL AND date_cancelled IS NULL)";
            case CONFIRMED:
                return "(date_confirmed IS NOT NULL AND date_ready IS NULL AND date_cancelled IS NULL)";
            case READY:
                return "(date_ready IS NOT NULL AND date_delivered IS NULL AND date_cancelled IS NULL)";
            case DELIVERED:
                return "date_delivered IS NOT NULL";
            default:
                return "date_cancelled IS NOT NULL";
        }
    }

    @Override
    public PaginatedResult<Order> get(Long userId, Long restaurantId, OrderStatus orderStatus, boolean onlyInProgress, boolean descending, int pageNumber, int pageSize) {
        Utils.validatePaginationParams(pageNumber, pageSize);

        final String orderDir = descending ? "DESC" : "ASC";
        final String statusCondString = getOrderStatusConditionString(orderStatus);
        final String inProgressCondString = getInProgressConditionString(onlyInProgress);

        final StringBuilder queryBuilder = new StringBuilder("SELECT order_id FROM orders");
        final StringBuilder countQueryBuilder = new StringBuilder("SELECT COUNT(*) FROM orders");
        String andToken = " WHERE";

        if (userId != null) {
            queryBuilder.append(andToken).append(" user_id = :userId");
            countQueryBuilder.append(andToken).append(" user_id = :userId");
            andToken = " AND";
        }

        if (restaurantId != null) {
            queryBuilder.append(andToken).append(" restaurant_id = :restaurantId");
            countQueryBuilder.append(andToken).append(" restaurant_id = :restaurantId");
            andToken = " AND";
        }

        if (statusCondString != null) {
            queryBuilder.append(andToken).append(' ').append(statusCondString);
            countQueryBuilder.append(andToken).append(' ').append(statusCondString);
            andToken = " AND";
        }

        if (inProgressCondString != null) {
            queryBuilder.append(andToken).append(' ').append(inProgressCondString);
            countQueryBuilder.append(andToken).append(' ').append(inProgressCondString);
            andToken = " AND";
        }

        queryBuilder.append(" ORDER BY date_ordered ").append(orderDir);

        final Query nativeQuery = em.createNativeQuery(queryBuilder.toString());
        final Query countQuery = em.createNativeQuery(countQueryBuilder.toString());
        nativeQuery.setMaxResults(pageSize);
        nativeQuery.setFirstResult((pageNumber - 1) * pageSize);

        if (userId != null) {
            nativeQuery.setParameter("userId", userId);
            countQuery.setParameter("userId", userId);
        }

        if (restaurantId != null) {
            nativeQuery.setParameter("restaurantId", restaurantId);
            countQuery.setParameter("restaurantId", restaurantId);
        }

        final List<Long> idList = nativeQuery.getResultList().stream().mapToLong(n -> ((Number) n).longValue()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        int count = ((Number) countQuery.getSingleResult()).intValue();

        if (idList.isEmpty())
            return new PaginatedResult<>(Collections.emptyList(), pageNumber, pageSize, count);

        final TypedQuery<Order> query = em.createQuery(
                "FROM Order WHERE orderId IN :idList ORDER BY dateOrdered " + orderDir,
                Order.class
        );
        query.setParameter("idList", idList);

        final List<Order> orders = query.getResultList();

        return new PaginatedResult<>(orders, pageNumber, pageSize, count);
    }

    @Override
    public List<Long> cancelNonDeliveredOrders(long restaurantId) {
        final Query nativeQuery = em.createQuery("SELECT orderId FROM Order WHERE restaurantId = :restaurantId AND dateDelivered IS NULL AND dateCancelled IS NULL");
        nativeQuery.setParameter("restaurantId", restaurantId);
        final List<Long> idList = nativeQuery.getResultList().stream().mapToLong(n -> ((Number) n).longValue()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (!idList.isEmpty()) {
            final Query updateQuery = em.createQuery("UPDATE Order SET dateCancelled = CURRENT_TIMESTAMP WHERE orderId IN :idList");
            updateQuery.setParameter("idList", idList);
            int count = updateQuery.executeUpdate();
            LOGGER.info("Cancelled {} pending orders of restaurant {}", count, restaurantId);
        } else {
            LOGGER.info("No pending orders to cancel for restaurant {}", restaurantId);
        }
        return idList;
    }
}
