(ns sada-abe.memoize-ttl
  (:require [clojure.core.memoize :refer [ttl]]
            [detijd.units :refer [unit->ms]]))

(defmacro defn-ttl [duration unit name & body]
  `(let [threshold# (* ~duration (unit->ms ~unit))]
     (def ~name (ttl (fn ~body) :ttl/threshold threshold#))))
