package me.tseng.studios.tchores.java;

import android.content.Context;
import android.text.TextUtils;

import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.util.ChoreUtil;

import com.google.firebase.firestore.Query;

/**
 * Object for passing filters around.
 */
public class Filters {

    private String uuid = null;
    private String city = null;
    private int price = -1;
    private String sortBy = null;
    private Query.Direction sortDirection = null;

    public Filters() {}

    public static Filters getDefault() {
        Filters filters = new Filters();
        filters.setSortBy(Chore.FIELD_ADTIME);
        filters.setSortDirection(Query.Direction.DESCENDING);

        return filters;
    }
    public static Filters getDefault(String currentUserName) {
        Filters filters = new Filters();
        filters.setSortBy(Chore.FIELD_ADTIME);
        filters.setSortDirection(Query.Direction.DESCENDING);
        filters.setUuid(currentUserName);

        return filters;
    }

    public boolean hasUuid() {
        return !(TextUtils.isEmpty(uuid));
    }

    public boolean hasCity() {
        return !(TextUtils.isEmpty(city));
    }

    public boolean hasPrice() {
        return (price > 0);
    }

    public boolean hasSortBy() {
        return !(TextUtils.isEmpty(sortBy));
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Query.Direction getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(Query.Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getSearchDescription(Context context) {
        StringBuilder desc = new StringBuilder();

        if (uuid == null && city == null) {
            desc.append("<b>");
            desc.append(context.getString(R.string.all_chores));
            desc.append("</b>");
        }

        if (uuid != null) {
            desc.append("<b>");
            desc.append(uuid);
            desc.append("</b>");
        }

        if (uuid != null && city != null) {
            desc.append(" in ");
        }

        if (city != null) {
            desc.append("<b>");
            desc.append(city);
            desc.append("</b>");
        }

        if (price > 0) {
            desc.append(" for ");
            desc.append("<b>");
            desc.append(ChoreUtil.getPriceString(price));
            desc.append("</b>");
        }

        return desc.toString();
    }

    public String getOrderDescription(Context context) {
        if (Chore.FIELD_PRICE.equals(sortBy)) {
            return context.getString(R.string.sorted_by_price);
        } else if (Chore.FIELD_POPULARITY.equals(sortBy)) {
            return context.getString(R.string.sorted_by_popularity);
        } else if (Chore.FIELD_ADTIME.equals(sortBy)) {
            return context.getString(R.string.sorted_by_aDTime);
        } else {
            return context.getString(R.string.sorted_by_rating);
        }
    }
}
