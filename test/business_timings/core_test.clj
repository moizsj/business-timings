(ns business-timings.core-test
  "Unit tests for business timings check functions."
  {:author "Moiz Jinia <moiz.jinia@gmail.com"}
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [business-timings.core :refer :all]))


(def test-business-timings
  {:business-days [true true false true false false true] ;; Mon-Sun
   :business-hours [;; Monday
                    [{:from {:hours 16 :minutes 0}
                      :to {:hours 17 :minutes 0}
                      :timezone "Asia/Kolkata"}
                     {:from {:hours 13 :minutes 0}
                      :to {:hours 18 :minutes 0}
                      :timezone "America/Los_Angeles"}]
                    ;; Tuesday
                    [{:from {:hours 8 :minutes 0}
                      :to {:hours 18 :minutes 0}
                      :timezone "Pacific/Kiritimati"}]
                    ;; Wednesday
                    []
                    ;; Thursday
                    [{:from {:hours 1 :minutes 0}
                      :to {:hours 3 :minutes 0}
                      :timezone "UTC"}
                     {:from {:hours 10 :minutes 0}
                      :to {:hours 18 :minutes 0}
                      :timezone "Asia/Kolkata"}]
                    ;; Friday
                    []
                    ;; Saturday
                    []
                    ;; Sunday
                    [{:from {:hours 9 :minutes 0}
                      :to {:hours 0 :minutes 0}
                      :timezone "US/Hawaii"}]]})


(deftest test-within-tomorrows-business-timings
  (testing "that next day's timings check is correct"
    (is (not (within-tomorrows-business-timings?
               test-business-timings
               (t/to-time-zone (t/from-time-zone
                                 (t/date-time 2016 8 30 7 30)
                                 (t/time-zone-for-id "Pacific/Kiritimati"))
                               (t/time-zone-for-id "US/Hawaii"))))
        "outside hours - next day with single timings")
    (is (within-tomorrows-business-timings?
          test-business-timings
          (t/to-time-zone (t/from-time-zone
                            (t/date-time 2016 8 30 8 30)
                            (t/time-zone-for-id "Pacific/Kiritimati"))
                          (t/time-zone-for-id "US/Hawaii")))
        "within hours - next day with single timings")
    (is (within-tomorrows-business-timings?
          test-business-timings
          (t/to-time-zone (t/from-time-zone (t/date-time 2016 9 1 10 30)
                                            (t/time-zone-for-id "Asia/Kolkata"))
                          (t/time-zone-for-id "US/Hawaii")))
        "within hours - next day with multiple timings")
    (is (within-tomorrows-business-timings?
          test-business-timings
          (t/to-time-zone (t/from-time-zone (t/date-time 2016 9 1 2 0)
                                            (t/time-zone-for-id "UTC"))
                          (t/time-zone-for-id "US/Hawaii")))
        "within hours - next day with multiple timings")
    (is (not (within-tomorrows-business-timings?
               test-business-timings
               (t/to-time-zone (t/from-time-zone (t/date-time 2016 9 1 0 30)
                                                 (t/time-zone-for-id "UTC"))
                               (t/time-zone-for-id "US/Hawaii"))))
        "outside hours - next day with multiple timings")
    (is (not (within-tomorrows-business-timings?
               test-business-timings
               (t/to-time-zone (t/from-time-zone (t/date-time 2016 9 3 10 30)
                                                 (t/time-zone-for-id "Asia/Kolkata"))
                               (t/time-zone-for-id "US/Hawaii"))))
        "next day is not a business day")))


(deftest test-within-yesterdays-business-timings
  (testing "that previois day's timings check is correct"
    (is (not (within-yesterdays-business-timings?
               test-business-timings
               (t/from-time-zone (t/date-time 2016 8 29 8 30)
                                 (t/time-zone-for-id "Pacific/Kiritimati"))))
        "outside hours - previous day with single timings")
    (is (within-yesterdays-business-timings?
          test-business-timings
          (t/from-time-zone (t/date-time 2016 8 29 9 30)
                            (t/time-zone-for-id "Pacific/Kiritimati")))
        "within hours - previous day with single timings")
    (is (within-yesterdays-business-timings?
          test-business-timings
          (t/from-time-zone (t/date-time 2016 8 30 10 30)
                            (t/time-zone-for-id "Pacific/Kiritimati")))
        "within hours - previous day with multiple timings")
    (is (within-yesterdays-business-timings?
          test-business-timings
          (t/from-time-zone (t/date-time 2016 8 30 1 30)
                            (t/time-zone-for-id "Pacific/Kiritimati")))
        "within hours - previous day with multiple timings")
    (is (not (within-yesterdays-business-timings?
               test-business-timings
               (t/from-time-zone (t/date-time 2016 8 30 2 30)
                                 (t/time-zone-for-id "Pacific/Kiritimati"))))
        "outside hours - previous day with multiple timings")
    (is (not (within-yesterdays-business-timings?
               test-business-timings
               (t/from-time-zone (t/date-time 2016 9 3 9 30)
                                 (t/time-zone-for-id "Pacific/Kiritimati"))))
        "previous day not a business day")))



(deftest test-within-todays-business-timings
  (testing "that current day's timings check is correct"
    (is (not (within-todays-business-timings?
               test-business-timings
               (t/from-time-zone (t/date-time 2016 8 30 7 30)
                                 (t/time-zone-for-id "Pacific/Kiritimati"))))
        "outside hours - current day with single timings")
    (is (within-todays-business-timings?
          test-business-timings
          (t/from-time-zone (t/date-time 2016 8 30 17 59)
                            (t/time-zone-for-id "Pacific/Kiritimati")))
        "within hours - current day with single timings")
    (is (within-todays-business-timings?
          test-business-timings
          (t/from-time-zone (t/date-time 2016 8 29 13 30)
                            (t/time-zone-for-id "America/Los_Angeles")))
        "within hours - current day with multiple timings")
    (is (within-todays-business-timings?
          test-business-timings
          (t/from-time-zone (t/date-time 2016 8 29 20 30)
                            (t/time-zone-for-id "UTC")))
        "within hours - current day with multiple timings")
    (is (not (within-todays-business-timings?
               test-business-timings
               (t/from-time-zone (t/date-time 2016 8 29 8 30)
                                 (t/time-zone-for-id "America/Los_Angeles"))))
        "outside hours - current day with multiple timings")
    (is (not (within-todays-business-timings?
               test-business-timings
               (t/from-time-zone (t/date-time 2016 8 31 6 30)
                                 (t/time-zone-for-id "America/Los_Angeles"))))
        "today is not a business day")))


(deftest test-within-business-timings
  (testing "that business timings check is correct"
    (is (within-business-timings?
          test-business-timings
          (t/to-time-zone (t/from-time-zone (t/date-time 2016 9 1 10 30)
                                            (t/time-zone-for-id "Asia/Kolkata"))
                          (t/time-zone-for-id "US/Hawaii")))
        "business timings check covers tomorrow")

    (is (within-business-timings?
          test-business-timings
          (t/from-time-zone (t/date-time 2016 8 30 1 30)
                            (t/time-zone-for-id "Pacific/Kiritimati")))
        "business timings check covers yesterday")

    (is (within-business-timings?
          test-business-timings
          (t/from-time-zone (t/date-time 2016 8 30 17 59)
                            (t/time-zone-for-id "Pacific/Kiritimati")))
        "business timings check cover today")))


(deftest test-all-day
  (testing "that all day range works correctly"
    (is (within-todays-business-timings?
          test-business-timings
          (t/from-time-zone (t/date-time 2016 9 18 9 30)
                            (t/time-zone-for-id "US/Hawaii")))
        "all day range works")
    (is (within-todays-business-timings?
          test-business-timings
          (t/from-time-zone (t/date-time 2016 9 18 23 59 59 992)
                            (t/time-zone-for-id "US/Hawaii")))
        "all day range includes last minute of the day")))
