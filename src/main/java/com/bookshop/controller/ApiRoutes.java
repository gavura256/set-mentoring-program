package com.bookshop.controller;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ApiRoutes {
    public static final String USERS = "/api/users";
    public static final String AUTH = "/api/auth";
    public static final String PRODUCTS = "/api/products";
    public static final String BOOKINGS = "/api/bookings";

    public static final String BY_ID = "/{id}";
    public static final String BY_USER_ID = "/user/{userId}";
    public static final String LOGIN = "/login";
    public static final String REGISTER = "/register";
}
