(ns sada-abe.constant-rate-alt
  (:require [clojure.core.async :as a :refer [<!!]]
            [detijd.units :refer [unit->ms]]))

(defn fn-throttle [rate unit]
  (let [delay (/ (unit->ms unit) rate)]
    (fn [f]
      (fn [& args]
        (<!! (a/timeout delay))
        (apply f args)))))

(defn throttle-fn [f rate unit]
  ((fn-throttle rate unit) f))
