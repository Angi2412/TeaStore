import json
import logging
from random import randint

from locust import HttpUser, task

# logging
logging.getLogger().setLevel(logging.DEBUG)


class UserBehavior(HttpUser):
    # database init
    number_of_categories = 5
    number_of_products_per_category = 100
    number_of_new_user = 100
    number_of_orders = 5
    number_of_products_per_page = 20

    @task
    def load(self) -> None:
        """
        Simulates user behaviour.
        :return: None
        """
        logging.debug("Starting load.")
        self.visit_home()
        self.login()
        self.browse()
        self.buy()
        self.visit_profile()
        self.logout()
        logging.debug("Completed load.")

    def visit_home(self) -> None:
        """
        Visits the landing page.
        :return: None
        """
        # load landing page
        res = self.client.get('/')
        if res.ok:
            logging.debug("Loaded landing page.")
        else:
            logging.error(f"Could not load landing page: {res.status_code}")

    def login(self) -> None:
        """
        User login with random userid between 1 and 90.
        :return: categories
        """
        # load login page
        res = self.client.get('/login')
        if res.ok:
            logging.debug("Loaded login page.")
        else:
            logging.error(f"Could not load login page: {res.status_code}")
        # login
        username = f"user{randint(1, (self.number_of_new_user - 10))}"
        credentials = {"username": username, "password": "password"}
        login_request = self.client.post("/loginAction", params=credentials)
        if login_request.ok:
            logging.debug(f"Login with username: {username}")
        else:
            logging.error(f"Could not login with username: {username} - status: {login_request.status_code}")

    def browse(self) -> None:
        """
        Simulates random browsing behaviour.
        :return: None
        """
        # execute browsing action randomly between 2 and 5 times
        for i in range(1, randint(2, self.number_of_orders)):
            category_id = randint(2, (self.number_of_categories + 1))
            page = randint(1, int((self.number_of_products_per_category / self.number_of_products_per_page)))
            category_request = self.client.get("/category", params={"page": page, "category": category_id})
            if category_request.ok:
                logging.debug(f"Visited category {category_id} on page 1")
                product_id = randint(7, (self.number_of_products_per_category + 6))
                product_request = self.client.get("/product", params={"id": product_id})
                if product_request.ok:
                    logging.debug(f"Visited product with id {product_id}.")
                    cart_request = self.client.post("/cartAction", params={"addToCart": "", "productid": product_id})
                    if cart_request.ok:
                        logging.debug(f"Added product {product_id} to cart.")
                    else:
                        logging.error(
                            f"Could not put product {product_id} in cart - status {product_request.status_code}")
                else:
                    logging.error(
                        f"Could not visit product {product_id} - status {product_request.status_code}")
            else:
                logging.error(
                    f"Could not visit category {category_id} on page 1 - status {category_request.status_code}")

    def buy(self) -> None:
        """
        Simulates to buy products in the cart with sample user data.
        :return: None
        """
        # sample user data
        user_data = {
            "firstname": "User",
            "lastname": "User",
            "adress1": "Road",
            "adress2": "City",
            "cardtype": "volvo",
            "cardnumber": "314159265359",
            "expirydate": "12/2050",
            "confirm": "Confirm"
        }
        buy_request = self.client.post("/cartAction", params=user_data)
        if buy_request.ok:
            logging.debug("Bought products.")
        else:
            logging.error("Could not buy products.")

    def visit_profile(self) -> None:
        """
        Visits user profile.
        :return: None
        """
        profile_request = self.client.get("/profile")
        if profile_request.ok:
            logging.debug("Visited profile page.")
        else:
            logging.error("Could not visit profile page.")

    def logout(self) -> None:
        """
        User logout.
        :return: None
        """
        logout_request = self.client.post("/loginAction", params={"logout": ""})
        if logout_request.ok:
            logging.debug("Successful logout.")
        else:
            logging.error(f"Could not log out - status: {logout_request.status_code}")
