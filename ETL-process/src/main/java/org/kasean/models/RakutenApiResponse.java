package org.kasean.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RakutenApiResponse {
    @JsonProperty("Items")
    private List<ItemWrapper> items;

    @JsonProperty("pageCount")
    private int pageCount;

    @JsonProperty("hits")
    private int totalItems;

    public List<ItemWrapper> getItems() {
        return items;
    }

    public void setItems(List<ItemWrapper> items) {
        this.items = items;
    }


    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemWrapper {
        @JsonProperty("Item")
        private Item item;

        public Item getItem() {
            return item;
        }

        public void setItem(Item item) {
            this.item = item;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JsonProperty("itemName")
        private String name;

        @JsonProperty("itemPrice")
        private Integer price;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
        }
    }
}
