package com.omegafrog.My.piano.app.web.domain.relation;

import com.omegafrog.My.piano.app.web.domain.order.SellableItem;

public interface UserPurchasedItem {
    SellableItem getItem();
}
