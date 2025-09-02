(ns robobots.api.api-test
  (:require [clojure.test :as test :refer :all]
            [robobots.api.api :as api]))

(deftest routes-parts
  (testing "api/parts"
    (is (some? (api/app {:uri "/api/parts"
                         :request-method :get}))))

  (testing "api/parts?<filter>=<value>"
    (is (some? (api/app {:uri "/api/parts"
                         :query-string "name=TODO"
                         :request-method :get}))))

  (testing "api/part/:id"
    (is (some? (api/app {:uri "/api/part/1"
                         :request-method :get}))))
  ,)

(deftest routes-user
  (testing "api/user/robobots"
    (is (some? (api/app {:uri "/api/user/robobots"
                         :request-method :get})))

    (is (some? (api/app {:uri "/api/user/robobots"
                         :request-method :post}))))

  (testing "api/user/robobots?<filter>=<value>"
    (is (some? (api/app {:uri "/api/user/robobots"
                         :query-string "name=TODO"
                         :request-method :get}))))

  (testing "api/user/robobot/:id"
    (is (some? (api/app {:uri "/api/user/robobot/1"
                         :request-method :get})))

    (is (some? (api/app {:uri "/api/user/robobot/1"
                         :request-method :patch})))

    (is (some? (api/app {:uri "/api/user/robobot/1"
                         :request-method :delete}))))

  (testing "api/user/tournaments"
    (is (some? (api/app {:uri "/api/user/tournaments"
                         :request-method :get}))))
  ,)

(deftest routes-tournament
  (testing "api/tournaments"
    (is (some? (api/app {:uri "/api/tournaments"
                         :request-method :get}))))

  (testing "api/tournaments?<filter>=<value>"
    (is (some? (api/app {:uri "/api/tournaments"
                         :query-string "name=TODO"
                         :request-method :get}))))

  (testing "api/tournament/:id"
    (is (some? (api/app {:uri "/api/tournament/1"
                         :request-method :get})))

    (is (some? (api/app {:uri "/api/tournament/1"
                         :request-method :put})))

    (is (some? (api/app {:uri "/api/tournament/1"
                         :request-method :delete}))))
  ,)

(deftest routes-auth
  (testing "auth/signup"
    (is (some? (api/app {:uri "/auth/signup"
                         :request-method :post}))))

  (testing "auth/login"
    (is (some? (api/app {:uri "/auth/login"
                         :request-method :post}))))

  (testing "auth/logout"
    (is (some? (api/app {:uri "/auth/logout"
                         :request-method :get}))))
  ,)
