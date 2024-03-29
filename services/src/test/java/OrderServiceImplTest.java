import ar.edu.itba.paw.exception.InvalidUserArgumentException;
import ar.edu.itba.paw.exception.OrderNotFoundException;
import ar.edu.itba.paw.model.Order;
import ar.edu.itba.paw.model.OrderStatus;
import ar.edu.itba.paw.model.OrderType;
import ar.edu.itba.paw.persistance.OrderDao;
import ar.edu.itba.paw.service.EmailService;
import ar.edu.itba.paw.services.OrderServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private final OrderServiceImpl orderServiceImpl = new OrderServiceImpl();

    private static final long DEFAULT_ORDER_ID = 1L;
    private static final LocalDateTime DEFAULT_DATE_TIME = LocalDateTime.now();
    private static final String DEFAULT_ADDRESS = "address";
    private static final int DEFAULT_TABLE_NUMBER = 10;

    @Test
    public void testMarkAsConfirmedValidOrder() {
        final Order order = spy(Order.class);
        order.setDateConfirmed(null);
        when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));
        when(order.getOrderStatus()).thenReturn(OrderStatus.PENDING);

        final Order result = orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.CONFIRMED);
        assertNotNull(result.getDateConfirmed());
    }

    @Test(expected = OrderNotFoundException.class)
    public void testMarkAsConfirmedInvalidOrder() {
        when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.empty());
        orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.CONFIRMED);
    }

    @Test(expected = InvalidUserArgumentException.class)
    public void testMarkAsConfirmedAlreadyConfirmedOrder() {
        final Order order = mock(Order.class);
        when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));
        when(order.getOrderStatus()).thenReturn(OrderStatus.CONFIRMED);

        orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.CONFIRMED);
    }

    @Test
    public void testMarkAsReadyValidOrder() {
        final Order order = spy(Order.class);
        order.setDateReady(null);
        when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));
        when(order.getOrderStatus()).thenReturn(OrderStatus.CONFIRMED);

        final Order result = orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.READY);
        assertNotNull(result.getDateReady());
    }

    @Test(expected = OrderNotFoundException.class)
    public void testMarkAsReadyInvalidOrder() {
        when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.empty());
        orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.READY);
    }

    @Test(expected = InvalidUserArgumentException.class)
    public void testMarkAsReadyAlreadyReadyOrder() {
        final Order order = mock(Order.class);
        when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));
        when(order.getOrderStatus()).thenReturn(OrderStatus.READY);

        orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.READY);
    }

    @Test
    public void testMarkAsDeliveredValidOrder() {
        final Order order = spy(Order.class);
        order.setDateDelivered(null);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));
         when(order.getOrderStatus()).thenReturn(OrderStatus.READY);

        final Order result = orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.DELIVERED);
        assertNotNull(result.getDateDelivered());
    }

    @Test(expected = OrderNotFoundException.class)
    public void testMarkAsDeliveredInvalidOrder() {
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.empty());
        orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.DELIVERED);
    }

    @Test(expected = InvalidUserArgumentException.class)
    public void testMarkAsDeliveredAlreadyDeliveredOrder() {
        final Order order = mock(Order.class);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));
         when(order.getOrderStatus()).thenReturn(OrderStatus.DELIVERED);

        orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.DELIVERED);
    }

    @Test
    public void testMarkAsCancelledConfirmedOrder() {
        final Order order = spy(Order.class);
        order.setDateCancelled(null);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));
         when(order.getOrderStatus()).thenReturn(OrderStatus.CONFIRMED);

        final Order result = orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.CANCELLED);
        assertNotNull(result.getDateCancelled());
    }

    @Test
    public void testMarkAsCancelledPendingOrder() {
        final Order order = spy(Order.class);
        order.setDateCancelled(null);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));
         when(order.getOrderStatus()).thenReturn(OrderStatus.PENDING);

        final Order result = orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.REJECTED);
        assertNotNull(result.getDateCancelled());
    }

    @Test(expected = InvalidUserArgumentException.class)
    public void testCantCancelDeliveredOrder() {
        final Order order = mock(Order.class);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));
         when(order.getOrderStatus()).thenReturn(OrderStatus.DELIVERED);

        orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.CANCELLED);
    }

    @Test(expected = InvalidUserArgumentException.class)
    public void testCantCancelAlreadyCancelledOrder() {
        final Order order = mock(Order.class);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));
         when(order.getOrderStatus()).thenReturn(OrderStatus.CANCELLED);

        orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.CANCELLED);
    }

    @Test(expected = InvalidUserArgumentException.class)
    public void testCantCancelRejectedOrder() {
        final Order order = mock(Order.class);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));
         when(order.getOrderStatus()).thenReturn(OrderStatus.REJECTED);

        orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.CANCELLED);
    }

    @Test(expected = OrderNotFoundException.class)
    public void testMarkAsCancelledInvalidOrder() {
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.empty());
        orderServiceImpl.advanceOrderStatus(DEFAULT_ORDER_ID, OrderStatus.CANCELLED);
    }

    @Test
    public void testSetOrderStatusPending() {
        final Order order = spy(Order.class);
        order.setDateConfirmed(DEFAULT_DATE_TIME);
        order.setDateReady(DEFAULT_DATE_TIME);
        order.setDateDelivered(DEFAULT_DATE_TIME);
        order.setDateCancelled(DEFAULT_DATE_TIME);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.setOrderStatus(DEFAULT_ORDER_ID, OrderStatus.PENDING);

        assertNull(order.getDateConfirmed());
        assertNull(order.getDateReady());
        assertNull(order.getDateDelivered());
        assertNull(order.getDateCancelled());
    }

    @Test
    public void testSetOrderStatusConfirmed() {
        final Order order = spy(Order.class);
        order.setDateConfirmed(DEFAULT_DATE_TIME);
        order.setDateReady(DEFAULT_DATE_TIME);
        order.setDateDelivered(DEFAULT_DATE_TIME);
        order.setDateCancelled(DEFAULT_DATE_TIME);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.setOrderStatus(DEFAULT_ORDER_ID, OrderStatus.CONFIRMED);

        assertNull(order.getDateDelivered());
        assertNull(order.getDateReady());
        assertNull(order.getDateCancelled());
        assertNotNull(order.getDateConfirmed());
    }

    @Test
    public void testSetOrderStatusReady() {
        final Order order = spy(Order.class);
        order.setDateConfirmed(DEFAULT_DATE_TIME);
        order.setDateReady(DEFAULT_DATE_TIME);
        order.setDateDelivered(DEFAULT_DATE_TIME);
        order.setDateCancelled(DEFAULT_DATE_TIME);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.setOrderStatus(DEFAULT_ORDER_ID, OrderStatus.READY);

        assertNull(order.getDateDelivered());
        assertNull(order.getDateCancelled());
        assertNotNull(order.getDateConfirmed());
        assertNotNull(order.getDateReady());
    }

    @Test
    public void testSetOrderStatusDelivered() {
        final Order order = spy(Order.class);
        order.setDateConfirmed(DEFAULT_DATE_TIME);
        order.setDateReady(DEFAULT_DATE_TIME);
        order.setDateDelivered(DEFAULT_DATE_TIME);
        order.setDateCancelled(DEFAULT_DATE_TIME);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.setOrderStatus(DEFAULT_ORDER_ID, OrderStatus.DELIVERED);

        assertNull(order.getDateCancelled());
        assertNotNull(order.getDateReady());
        assertNotNull(order.getDateConfirmed());
        assertNotNull(order.getDateDelivered());
    }

    @Test
    public void testSetOrderStatusCancelled() {
        final Order order = spy(Order.class);
        order.setDateConfirmed(DEFAULT_DATE_TIME);
        order.setDateReady(DEFAULT_DATE_TIME);
        order.setDateDelivered(DEFAULT_DATE_TIME);
        order.setDateCancelled(DEFAULT_DATE_TIME);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.setOrderStatus(DEFAULT_ORDER_ID, OrderStatus.CANCELLED);

        assertNull(order.getDateDelivered());
        assertNotNull(order.getDateReady());
        assertNotNull(order.getDateConfirmed());
        assertNotNull(order.getDateCancelled());
    }

    @Test
    public void testSetOrderStatusRejected() {
        final Order order = spy(Order.class);
        order.setDateCancelled(DEFAULT_DATE_TIME);
        order.setDateConfirmed(DEFAULT_DATE_TIME);
        order.setDateReady(DEFAULT_DATE_TIME);
        order.setDateDelivered(DEFAULT_DATE_TIME);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.setOrderStatus(DEFAULT_ORDER_ID, OrderStatus.REJECTED);

        assertNull(order.getDateConfirmed());
        assertNull(order.getDateReady());
        assertNull(order.getDateDelivered());
        assertEquals(DEFAULT_DATE_TIME, order.getDateCancelled());
    }

    @Test(expected = OrderNotFoundException.class)
    public void testSetInvalidOrder() {
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.empty());

        orderServiceImpl.setOrderStatus(DEFAULT_ORDER_ID, OrderStatus.CONFIRMED);
    }

    @Test
    public void testUpdateAddressValidAddress() {
        final Order order = spy(Order.class);
        order.setOrderType(OrderType.DELIVERY);
        order.setDateOrdered(DEFAULT_DATE_TIME);

         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.updateAddress(DEFAULT_ORDER_ID, DEFAULT_ADDRESS);

        assertEquals(DEFAULT_ADDRESS, order.getAddress());
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateAddressTakeAway() {
        final Order order = mock(Order.class);
         when(order.getOrderType()).thenReturn(OrderType.TAKEAWAY);

         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.updateAddress(DEFAULT_ORDER_ID, DEFAULT_ADDRESS);
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateAddressDineIn() {
        final Order order = mock(Order.class);
         when(order.getOrderType()).thenReturn(OrderType.DINE_IN);

         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.updateAddress(DEFAULT_ORDER_ID, DEFAULT_ADDRESS);
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateAddressClosedOrder() {
        final Order order = spy(Order.class);
        order.setOrderType(OrderType.DELIVERY);
        order.setDateConfirmed(DEFAULT_DATE_TIME);
        order.setDateReady(DEFAULT_DATE_TIME);
        order.setDateDelivered(DEFAULT_DATE_TIME);

         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.updateAddress(DEFAULT_ORDER_ID, DEFAULT_ADDRESS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAddressNullAddress() {
        final Order order = spy(Order.class);
        order.setOrderType(OrderType.DELIVERY);

         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.updateAddress(DEFAULT_ORDER_ID, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAddressBlankAddress() {
        final Order order = spy(Order.class);
        order.setOrderType(OrderType.DELIVERY);

         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.updateAddress(DEFAULT_ORDER_ID, "");
    }

    @Test
    public void testUpdateTableNumberValidTableNumber() {
        final Order order = spy(Order.class);
        order.setOrderType(OrderType.DINE_IN);
        order.setDateOrdered(DEFAULT_DATE_TIME);
         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.updateTableNumber(DEFAULT_ORDER_ID, DEFAULT_TABLE_NUMBER);

        assertEquals(DEFAULT_TABLE_NUMBER, order.getTableNumber().intValue());
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateTableNumberDeliveryOrder() {
        final Order order = spy(Order.class);
        order.setOrderType(OrderType.DELIVERY);
        order.setDateOrdered(DEFAULT_DATE_TIME);

         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.updateTableNumber(DEFAULT_ORDER_ID, DEFAULT_TABLE_NUMBER);
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateTableNumberTakeawayOrder() {
        final Order order = spy(Order.class);
        order.setOrderType(OrderType.TAKEAWAY);
        order.setDateOrdered(DEFAULT_DATE_TIME);

         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.updateTableNumber(DEFAULT_ORDER_ID, DEFAULT_TABLE_NUMBER);
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateTableNumberClosedOrder() {
        final Order order = spy(Order.class);
        order.setOrderType(OrderType.DINE_IN);
        order.setDateOrdered(DEFAULT_DATE_TIME);
        order.setDateConfirmed(DEFAULT_DATE_TIME);
        order.setDateReady(DEFAULT_DATE_TIME);
        order.setDateDelivered(DEFAULT_DATE_TIME);

         when(orderDao.getById(DEFAULT_ORDER_ID)).thenReturn(Optional.of(order));

        orderServiceImpl.updateTableNumber(DEFAULT_ORDER_ID, DEFAULT_TABLE_NUMBER);
    }
}
