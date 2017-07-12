(ns sada-abe.constant-rate-alt
  (:require [clojure.core.async :as a]))

(defn fn-throttle [delay]
  (fn [f]
    (fn [& args]
      (<!! (a/timeout delay))
      (apply f args))))

(defn throttle-fn [f delay]
  ((fn-throttle delay) f))
