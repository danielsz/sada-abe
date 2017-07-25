(ns sada-abe.constant-rate
  (:require [detijd.units :refer [unit->ms]])
  (:import [java.util.concurrent DelayQueue Delayed TimeUnit]))

(defprotocol DelayedElement
  (get-expiry-time [_])
  (equal-fn? [_ o])
  (get-id [_])
  (get-name [_])
  (run [_]))

(defn new-delayed-fn [queue delay f & args]
  (let [expiry-time (if-let [xs (seq queue)]
                      (+ (get-expiry-time (last xs)) delay)
                      (+ (System/currentTimeMillis) delay))
        id (str f)]
    (reify
      Delayed
      (getDelay [_ time-unit]
        (let [diff (- expiry-time (System/currentTimeMillis))]
          (.convert time-unit diff TimeUnit/MILLISECONDS)))
      Comparable
      (compareTo [_ o]
        (cond
          (< expiry-time (get-expiry-time o)) -1
          (> expiry-time (get-expiry-time o)) 1
          (= expiry-time (get-expiry-time o)) 0))
      DelayedElement
      (get-expiry-time [_]
        expiry-time)
      (get-id [_] id)
      (get-name [_] (clojure.main/demunge id))
      (run [_] (apply f (first args)))
      (equal-fn? [_ o]
        (= id (get-id o))))))

(defn fn-throttle [rate unit]
  (let [queue (DelayQueue.)
        delay (/ (unit->ms unit) rate)]
    (fn [f]
      (fn [& args]
        (let [el (new-delayed-fn queue delay f args)]
          (.put queue el)
          (run (.take queue)))))))

(defn throttle-fn [f rate unit]
  ((fn-throttle rate unit) f))
