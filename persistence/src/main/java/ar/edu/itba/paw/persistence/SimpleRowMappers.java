package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.*;
import ar.edu.itba.paw.model.util.AverageCountPair;
import ar.edu.itba.paw.model.util.Pair;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

class SimpleRowMappers {

    static LocalDateTime timestampToLocalDateTimeOrNull(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    static final RowMapper<Integer> COUNT_ROW_MAPPER = (rs, i) -> rs.getInt("c");

    static final RowMapper<Integer> MAX_ROW_MAPPER = (rs, i) -> rs.getInt("m");

    static final RowMapper<AverageCountPair> AVERAGE_COUNT_ROW_MAPPER = (rs, i) -> new AverageCountPair(
            rs.getFloat("a"),
            rs.getInt("c")
    );

    static final RowMapper<User> USER_ROW_MAPPER = (ResultSet rs, int rowNum) -> new User(
            rs.getInt("user_id"),
            rs.getString("user_email"),
            rs.getString("user_name"),
            rs.getInt("user_image_id"),
            rs.getBoolean("user_is_active")
    );

    static final RowMapper<Pair<User, String>> USER_WITH_PASSWORD_ROW_MAPPER = (ResultSet rs, int rowNum) -> new Pair<>(
            USER_ROW_MAPPER.mapRow(rs, rowNum),
            rs.getString("password")
    );

    static final RowMapper<byte[]> IMAGE_ROW_MAPPER = (ResultSet rs, int rowNum) -> rs.getBytes("bytes");


    static final RowMapper<Restaurant> RESTAURANT_ROW_MAPPER = (ResultSet rs, int rowNum) -> new Restaurant(
            rs.getInt("restaurant_id"),
            rs.getString("restaurant_name"),
            rs.getString("restaurant_email"),
            rs.getInt("restaurant_logo_id"),
            rs.getInt("restaurant_portrait_1_id"),
            rs.getInt("restaurant_portrait_2_id"),
            rs.getString("restaurant_address"),
            rs.getString("restaurant_description"),
            rs.getInt("restaurant_max_tables"),
            rs.getBoolean("restaurant_is_active")
    );

    static final RowMapper<RestaurantRoleLevel> RESTAURANT_ROLE_LEVEL_ROW_MAPPER = (ResultSet rs, int rowNum) -> {
        // If the is_owner is set to null in the result set, getBoolean returns false, so no need to worry here.
        if (rs.getBoolean("is_owner"))
            return RestaurantRoleLevel.OWNER;

        int ordinal = rs.getInt("role_level");
        RestaurantRoleLevel[] values = RestaurantRoleLevel.values();
        if (rs.wasNull() || ordinal < 0 || ordinal >= values.length)
            return null;
        return values[ordinal];
    };

    static final RowMapper<Category> CATEGORY_ROW_MAPPER = (ResultSet rs, int rowNum) -> new Category(
            rs.getInt("category_id"),
            RESTAURANT_ROW_MAPPER.mapRow(rs, rowNum),
            rs.getString("category_name"),
            rs.getInt("category_order")
    );

    static final RowMapper<Product> PRODUCT_ROW_MAPPER = (ResultSet rs, int rowNum) -> new Product(
            rs.getInt("product_id"),
            CATEGORY_ROW_MAPPER.mapRow(rs, rowNum),
            rs.getString("product_name"),
            rs.getBigDecimal("product_price"),
            rs.getString("product_description"),
            rs.getInt("product_image_id"),
            rs.getBoolean("product_available")
    );

    static final RowMapper<OrderItemless> ORDER_ITEMLESS_ROW_MAPPER = (ResultSet rs, int rowNum) -> new OrderItemless(
            rs.getInt("order_id"),
            OrderType.values()[rs.getInt("order_type")],
            RESTAURANT_ROW_MAPPER.mapRow(rs, rowNum),
            USER_ROW_MAPPER.mapRow(rs, rowNum),
            timestampToLocalDateTimeOrNull(rs.getTimestamp("order_date_ordered")),
            timestampToLocalDateTimeOrNull(rs.getTimestamp("order_date_confirmed")),
            timestampToLocalDateTimeOrNull(rs.getTimestamp("order_date_ready")),
            timestampToLocalDateTimeOrNull(rs.getTimestamp("order_date_delivered")),
            timestampToLocalDateTimeOrNull(rs.getTimestamp("order_date_cancelled")),
            rs.getString("order_address"),
            rs.getInt("order_table_number"),
            rs.getInt("order_item_count"),
            rs.getBigDecimal("order_price")
    );

    static final RowMapper<OrderItem> ORDER_ITEM_ROW_MAPPER = (ResultSet rs, int rowNum) -> new OrderItem(
            PRODUCT_ROW_MAPPER.mapRow(rs, rowNum),
            rs.getInt("order_item_line_number"),
            rs.getInt("order_item_quantity"),
            rs.getString("order_item_comment")
    );

    static final RowMapper<Review> ORDER_REVIEW_ROW_MAPPER = (ResultSet rs, int rowNum) -> new Review(
            ORDER_ITEMLESS_ROW_MAPPER.mapRow(rs, rowNum),
            rs.getInt("order_review_rating"),
            timestampToLocalDateTimeOrNull(rs.getTimestamp("order_review_date")),
            rs.getString("order_review_comment")
    );
}
