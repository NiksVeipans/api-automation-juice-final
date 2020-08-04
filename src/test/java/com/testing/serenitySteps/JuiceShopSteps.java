package com.testing.serenitySteps;

import com.testing.requestBodies.BaseRequestBody;
import cucumber.api.DataTable;
import io.restassured.response.Response;
import net.thucydides.core.annotations.Step;

import java.io.IOException;
import java.util.*;

import static net.serenitybdd.core.Serenity.sessionVariableCalled;
import static net.serenitybdd.core.Serenity.setSessionVariable;

@SuppressWarnings("unchecked")
public class JuiceShopSteps extends BaseSteps {
  private final static String _API_USERS_ = "/api/Users/";
  private final static String _REST_USER_LOGIN_ = "/rest/user/login/";

  @Step
  public static void getBasketContent(){
    sendRequest(GET, "/rest/basket/" + sessionVariableCalled("basket_id"));
  }

  @Step
  public static void addItemToBasket(DataTable dataTable) throws IOException {
    BaseRequestBody requestBody = createBodyCustom(dataTable);
    requestBody.addKey("BasketId", sessionVariableCalled("basket_id").toString());
    sendRequestWithBodyJson(POST, "/api/BasketItems/", requestBody.getBody());
  }

  @Step
  public static void createAnAccount(DataTable dataTable) throws IOException {
    Map<String, Object> map = new HashMap<>(dataTable.asMap(String.class, String.class));

    if(map.get("email").toString().equals(RANDOM_EMAIL)){
      String randomEmail = "test+" + new Random().nextInt(999999) + "@testdevlab.com";
      map.replace("email", randomEmail);
      setSessionVariable(RANDOM_EMAIL).to(randomEmail);
    }

    sendRequestWithBodyJson(POST, _API_USERS_, createBody(map));
    if (((Response) sessionVariableCalled(RESPONSE)).statusCode() == 201) {
      saveValueInPathToSessionVariable("data --> id", "user_id");
    }
  }

  @Step
  public static void logInAnAccount(DataTable dataTable) throws IOException {
    sendRequestWithBodyJson(POST, _REST_USER_LOGIN_, createBody(handleRandomEmail(dataTable)));
    if (((Response) sessionVariableCalled(RESPONSE)).statusCode() == 200){
      saveValueInPathToSessionVariable("authentication --> token", "token");
      saveValueInPathToSessionVariable("authentication --> bid", "basket_id");
    }
  }

  @Step
  public static void changePassword(DataTable dataTable) throws IOException {
    Map<String, String> requestData = dataTable.asMap(String.class, String.class);
    sendRequestWithBodyJson(GET, createChangePasswordEndpoint(
            requestData.get("current"),
            requestData.get("new"),
            requestData.get("repeat")), "{}");
  }

  @Step
  public static void resetPassword(DataTable dataTable) throws IOException {
    sendRequestWithBodyJson(POST, "/rest/user/reset-password", createBody(handleRandomEmail(dataTable)));
  }

  @Step
  public static void purchaseTheItems(DataTable dataTable) throws IOException {
    BaseRequestBody requestBody = createBodyCustom(dataTable);

    // Add payment id
    requestBody.addKey("orderDetails --> paymentId", sessionVariableCalled("payment_id").toString());
    // Add address id
    requestBody.addKey("orderDetails --> addressId", sessionVariableCalled("address_id").toString());
    sendRequestWithBodyJson(
            POST,
            // Add basket id
            createBasketCheckoutEndpoint(sessionVariableCalled("basket_id").toString()),
            requestBody.getBody());
  }

  @Step
  public static void userRequestsErasure(DataTable dataTable) throws IOException {
    sendRequestWithBodyJson(POST, "/rest/user/erasure-request", createBody(handleRandomEmail(dataTable)));
  }

  @Step
  public static void userAddsAnAddress(DataTable dataTable) throws IOException {
    sendRequestWithBodyJson(POST, "/api/Addresss/", createBody(dataTable));
    if (((Response) sessionVariableCalled(RESPONSE)).statusCode() == 201) {
      saveValueInPathToSessionVariable("data --> id", "address_id");
      saveValueInPathToSessionVariable("data --> id", "payment_id");
    }
  }

  @Step
  public static void userRequestsDeliveryOptions() throws IOException {
    sendRequestWithBodyJson(GET, "/api/Deliverys", "{}");
  }

  @Step
  public static void userAddsACreditCard(DataTable dataTable) throws IOException {
    sendRequestWithBodyJson(POST, "/api/Cards/", createBody(dataTable));
  }

  @Step
  public static void userSendsSecurityAnswer(DataTable dataTable) throws IOException {
    BaseRequestBody requestBody = createBodyCustom(dataTable);
    // Adding user_id
    requestBody.addKey("UserId", sessionVariableCalled("user_id").toString());

    sendRequestWithBodyJson(POST, "/api/SecurityAnswers/", requestBody.getBody());
  }

  // Private

  private static String createChangePasswordEndpoint(String current, String new_, String repeat){
    return "/rest/user/change-password?current=" + current + "&new=" + new_ + "&repeat=" + repeat;
  }

  private static String createBasketCheckoutEndpoint(String basketId){
    return "/rest/basket/" + basketId + "/checkout";
  }

  private static Map<String, Object> handleRandomEmail(DataTable dataTable){
    Map<String, Object> map = new HashMap<>(dataTable.asMap(String.class, String.class));
    if(map.get("email").toString().equals(RANDOM_EMAIL)){
      map.replace("email", sessionVariableCalled(RANDOM_EMAIL));
    }
    return map;
  }
}
