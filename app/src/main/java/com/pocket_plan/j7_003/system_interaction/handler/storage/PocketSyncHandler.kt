package com.pocket_plan.j7_003.system_interaction.handler.storage

import com.pocket_plan.j7_003.data.shoppinglist.model.dtos.ShoppingItemDto
import com.pocket_plan.j7_003.data.shoppinglist.model.dtos.ShoppingListDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface PocketSyncHandler {

    @HEAD("/api/v1/shopping-lists/{id}")
    fun checkConnection(@Path("id") id: String): Call<Unit>

    @POST("/api/v1/shopping-lists")
    fun syncShoppingList(@Body newShoppingList: ShoppingListDto): Call<ShoppingListDto>

    @POST("/api/v1/shopping-lists/{id}/{category}/items")
    fun addItemToList(
        @Path("id") id: String,
        @Path("category") category: String,
        @Body newShoppingItem: ShoppingItemDto
    ): Call<ShoppingItemDto>

    @GET("/api/v1/shopping-lists/{listName}")
    fun getShoppingLists(@Path("listName") listName: String): Call<Collection<ShoppingListDto>>

    @GET("/api/v1/shopping-lists/{id}")
    fun getShoppingList(@Path("id") id: String): Call<ShoppingListDto>

    @HTTP(method = "DELETE", path = "/api/v1/shopping-lists/{id}/{category}/items", hasBody = true)
    fun deleteItem(
        @Path("id") id: String,
        @Path("category") category: String,
        @Body item: ShoppingItemDto
    ): Call<Unit>
}