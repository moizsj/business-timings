# business-timings

A Clojure library that provides an API to check whether the specified time falls in business days and hours. Supports multiple business hour slots for a given day in different timezones. Accounts for the possibility that there could be time zones ahead or behind the server time that could be within business hours.

### Example use case

Customer support teams of large global organizations have teams working in multiple geographies across multiple time zones. Each of these teams usually have their own set of business days and hours (in their respective time zones).

Below is a Clojure map that represents an example business hours and days configuration for an org -

```clojure
(def example-business-timings
  {:business-days [true true false true true false true] ;; Mon-Sun
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
                    [{:from {:hours 0 :minutes 0} ;;all day
                      :to {:hours 0 :minutes 0}
                      :timezone "US/Hawaii"}]
                    ;; Saturday
                    []
                    ;; Sunday
                    [{:from {:hours 9 :minutes 0}
                      :to {:hours 17 :minutes 0}
                      :timezone "US/Hawaii"}]]})
```

When a support ticket from a customer comes in, we need an automated way to check whether any of the global support teams are within business hours.

## Usage

```clojure
(within-business-timings? example-business-timings (t/now))
```

## How this works
This blog post goes into the details of the use case and the logic behind the implementation.
[https://engineering.helpshift.com/2016/timezones-FP/](https://engineering.helpshift.com/2016/timezones-FP/)

## TODO
add ability to tell how far away in hours and minutes is the next available business hours slot (PRs welcome!)

## License

Copyright © 2016 Moiz Jinia

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
