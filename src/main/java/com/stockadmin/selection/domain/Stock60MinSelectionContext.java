package com.stockadmin.selection.domain;

import java.util.Collections;
import java.util.Set;

public class Stock60MinSelectionContext
{
    private final Integer latestSlotIndex;
    private final Set<Integer> existingSlots;

    public Stock60MinSelectionContext(Integer latestSlotIndex, Set<Integer> existingSlots)
    {
        this.latestSlotIndex = latestSlotIndex;
        this.existingSlots = existingSlots == null ? Collections.<Integer>emptySet() : existingSlots;
    }

    public Integer getLatestSlotIndex()
    {
        return latestSlotIndex;
    }

    public Set<Integer> getExistingSlots()
    {
        return existingSlots;
    }
}
