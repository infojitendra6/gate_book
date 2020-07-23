package com.kopykitab.gate.components.adapters;

import com.kopykitab.gate.models.StoreBannerItem;
import com.kopykitab.gate.models.StoreCategoryItem;
import com.kopykitab.gate.models.StoreCategorySection;
import com.kopykitab.gate.models.StoreRecommendationsItem;
import com.kopykitab.gate.models.StoreSearchItem;

public interface StoreItemClickListener {
    void itemClicked(StoreSearchItem search);

    void itemClicked(StoreBannerItem banner);

    void itemClicked(StoreRecommendationsItem recommendation);

    void itemClicked(StoreCategorySection section);

    void itemClicked(StoreCategoryItem item);
}
