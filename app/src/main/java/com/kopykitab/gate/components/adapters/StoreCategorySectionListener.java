package com.kopykitab.gate.components.adapters;

import com.kopykitab.gate.models.StoreCategorySection;

/**
 * interface to listen changes in state of sections
 */
public interface StoreCategorySectionListener {
    void onSectionStateChanged(StoreCategorySection section, boolean isOpen);
}