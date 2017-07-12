(ns sada-abe.token-bucket
  (:require [clojure.core.async :as a :refer [<! <!! >! >!! chan thread go go-loop put! timeout]]
            [sada-abe.core :refer [unit->ms]]))

(defn bucket
  ([burstiness]
   (bucket burstiness 10))
  ([burstiness delay]
   (let [c (if (zero? burstiness)
             (chan)
             (chan (a/dropping-buffer burstiness)))]
     (go-loop []
       (>! c :token)
       (<! (timeout delay))
       (recur))
     c)))

(defn throttle-chan [c bucket token-value]
  (let [c' (chan)]
    (go-loop [] 
      (<! bucket)
      (dotimes [_ token-value]
        (let [v (<! c)]
          (if (some? v)
            (>! c' v)
            (do (a/close! bucket)
                (a/close! c')))))
      (recur)) 
    c'))

(defn fn-throttle
  ([rate unit]
   (fn-throttle rate unit 0))
  ([rate unit burstiness]
   (let [in (chan)
         delay (/ (unit->ms unit) rate)
         out (if (< delay 10)
               (throttle-chan in (bucket burstiness) (* 10 delay))
               (throttle-chan in (bucket burstiness delay) 1))]
     (fn [f]
       (fn [& args]
         (>!! in :eval-request)
         (<!! out)
         (apply f args))))))

(defn throttle-fn
  ([f rate unit] 
   (throttle-fn f rate unit 0))
  ([f rate unit burstiness]
   ((fn-throttle rate unit burstiness) f)))

