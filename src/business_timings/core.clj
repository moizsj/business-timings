(ns business-timings.core
  ^{:doc "Provides timezone aware functions to check whether the
          specified time falls in business days and hours.

          Supports multiple business hours for a given day in
          different timezones.

          Accounts for the two additional possibilities that -
          its still yesterday in some time zone, or
          its already tomorrow in some time zone."
    :author "Moiz Jinia <moiz.jinia@gmail.com>"}
  (:require [clj-time.core :as t]))


(def example-business-timings
  {:business-days [true true false true true false true] ;; Mon-Sun
   :business-hours [;; Monday
                    [{:from {:hours 16 :minutes 0}
                      :to {:hours 17 :minutes 0}
                      :timezone "Asia/Kolkata"}
                     {:from {:hours 9 :minutes 0}
                      :to {:hours 23 :minutes 0}
                      :timezone "America/Los_Angeles"}]
                    ;; Tuesday
                    [{:from {:hours 1 :minutes 0}
                      :to {:hours 12 :minutes 0}
                      :timezone "Pacific/Kiritimati"}]
                    ;; Wednesday
                    [{:from {:hours 6 :minutes 0}
                      :to {:hours 12 :minutes 30}
                      :timezone "America/Los_Angeles"}]
                    ;; Thursday
                    [{:from {:hours 3 :minutes 0}
                      :to {:hours 6 :minutes 0}
                      :timezone "UTC"}
                     {:from {:hours 8 :minutes 0}
                      :to {:hours 18 :minutes 0}
                      :timezone "Asia/Kolkata"}]
                    ;; Friday
                    [{:from {:hours 0 :minutes 0} ;;all day
                      :to {:hours 0 :minutes 0}
                      :timezone "US/Hawaii"}]
                    ;; Saturday
                    [{:from {:hours 9 :minutes 0}
                      :to {:hours 12 :minutes 0}
                      :timezone "Asia/Kolkata"}]
                    ;; Sunday
                    [{:from {:hours 0 :minutes 0} ;;all day
                      :to {:hours 0 :minutes 0}
                      :timezone "US/Hawaii"}]]})


(defn- today
  [dt]
  (t/day-of-week dt))


(defn- tomorrow
  [dt]
  (t/day-of-week (t/plus dt (t/days 1))))


(defn- yesterday
  [dt]
  (t/day-of-week (t/minus dt (t/days 1))))


(defn- business-day?
  [day business-timings]
  ;; decerement day for zero based index
  (get (:business-days business-timings) (dec day)))


(defn- timings-for-day
  [day business-timings]
  ;; decerement day for zero based index
  (get (:business-hours business-timings) (dec day)))


(defn- timings-today
  [business-timings dt]
  (timings-for-day (today dt) business-timings))


(defn- timings-tomorrow
  [business-timings dt]
  (timings-for-day (tomorrow dt) business-timings))


(defn- timings-yesterday
  [business-timings dt]
  (timings-for-day (yesterday dt) business-timings))


(defn- business-day-today?
  [business-timings dt]
  (business-day? (today dt) business-timings))


(defn- business-day-tomorrow?
  [business-timings dt]
  (business-day? (tomorrow dt) business-timings))


(defn- business-day-yesterday?
  [business-timings dt]
  (business-day? (yesterday dt) business-timings))


(defn- business-hours-start-today
  [time-slot dt]
  (t/from-time-zone
    (t/date-time (t/year dt) (t/month dt) (t/day dt)
                 (get-in time-slot [:from :hours])
                 (get-in time-slot [:from :minutes]))
    (t/time-zone-for-id (:timezone time-slot))))


(defn- business-hours-end-today
  [time-slot dt]
  (let [end-hour (get-in time-slot [:to :hours])
        end-minute (get-in time-slot [:to :minutes])]
    (t/from-time-zone
      (if (every? zero? [end-hour end-minute])
        (t/date-time (t/year dt) (t/month dt) (t/day dt) 23 59 59 999)
        (t/date-time (t/year dt) (t/month dt) (t/day dt) end-hour end-minute))
      (t/time-zone-for-id (:timezone time-slot)))))


(defn- busines-hours-start-yesterday
  [time-slot dt]
  (t/minus (business-hours-start-today time-slot dt) (t/days 1)))


(defn- business-hours-end-yesterday
  [time-slot dt]
  (t/minus (business-hours-end-today time-slot dt) (t/days 1)))


(defn- business-hours-start-tomorrow
  [time-slot dt]
  (t/plus (business-hours-start-today time-slot dt) (t/days 1)))


(defn- business-hours-end-tomorrow
  [time-slot dt]
  (t/plus (business-hours-end-today time-slot dt) (t/days 1)))


(defn within-todays-business-timings?
  [business-timings dt]
  (when (business-day-today? business-timings dt)
    (let [time-slots (timings-today business-timings dt)]
      (some (fn [time-slot]
              (t/within? (business-hours-start-today time-slot dt)
                         (business-hours-end-today time-slot dt)
                         dt))
            time-slots))))


(defn within-tomorrows-business-timings?
  [business-timings dt]
  (when (business-day-tomorrow? business-timings dt)
    (let [time-slots (timings-tomorrow business-timings dt)]
      (some (fn [time-slot]
              (t/within? (business-hours-start-tomorrow time-slot dt)
                         (business-hours-end-tomorrow time-slot dt)
                         dt))
            time-slots))))


(defn within-yesterdays-business-timings?
  [business-timings dt]
  (when (business-day-yesterday? business-timings dt)
    (let [time-slots (timings-yesterday business-timings dt)]
      (some (fn [time-slot]
              (t/within? (busines-hours-start-yesterday time-slot dt)
                         (business-hours-end-yesterday time-slot dt)
                         dt))
            time-slots))))


(defn within-business-timings?
  [business-timings dt]
  (or (within-todays-business-timings? business-timings dt)
      (within-tomorrows-business-timings? business-timings dt)
      (within-yesterdays-business-timings? business-timings dt)))


(comment (within-business-timings? example-business-timings (t/now)))