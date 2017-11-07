(ns recurrent-ui.core
    (:require
      elmalike.mouse
      [dommy.core :as dommy]
      [elmalike.signal :as elmalike]
      [garden.core :as garden]
      [recurrent.core :as recurrent]
      [recurrent.drivers.css :include-macros true]
      [recurrent.drivers.dom :include-macros true]))

(enable-console-print!)

(println "This text is printed from src/recurrent-ui/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(def COLORS
  {:error "#FE001E"
   :primary "#FB00A7"
   :primary-alt {:dark "#7f0056"
                 :darker "#40002b"}
   :secondary "#00F4CB"
   :success "#00F4CB"
   :warning "#FFCD00"
   :white "#FFFFFF"
   :grey
   {:dark "#182028"
    :darkest "#081018"
    :standard "#606468"
    :light "#C0C4C8"
    :lightest "#F6F8FA"
    :lightest-v2 "#F6F8FA"}})
  
  
(defonce app-state (atom {:text "Hello world!"}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

; SOURCES
; dom-$
; label-$
; is-error?-$
; placeholder-$

; SINKS
; dom-$
; css-$
; value-$

(elmalike.mouse/initialize!)

(defn Button
  [props sources]
  (let [scope (gensym)
        dom-source-$ (recurrent.drivers.dom/isolate-source scope (:dom-$ sources))]
    {:clicks-$ ((dom-source-$ ".button") "click")
     :css-$ (elmalike/signal-of
              [:.button {:border "1px solid black"
                         :cursor "pointer"
                         :font-family "sans-serif"
                         :font-size "12px"
                         :font-weight "bold"
                         :height "40px"
                         :line-height "40px"
                         :letter-spacing "2px"
                         :text-align "center"
                         :text-transform "uppercase"}
               [:&.filled {:color "white" 
                           :border "none"
                           :box-shadow "0px 2px 8px rgba(0,0,0,.33)"}]
               [:&.primary {:background (:primary COLORS)}]
               [:&.pill {:border-radius "1600px"}]])
     :dom-$ (elmalike/map
              (fn [[text]]
                `[:div {:class ~(str "button" (if (:pill? props) " pill") (if (:primary? props) " filled primary"))} ~text])
              (elmalike/latest (:text-$ sources)))}))

(defn TextInput
  [sources]
  (let [scope (gensym)
        dom-source-$ (recurrent.drivers.dom/isolate-source scope (:dom-$ sources))
        value-$ 
        (elmalike/start-with "start!"
                             (elmalike/map
                               (fn [e] 
                                 (.-value (.-selectedTarget e)))
                               ((dom-source-$ "input") "keydown")))]
    {:value-$ value-$
     :css-$ (elmalike/map
              (fn [[left-icon right-icon]]
                [:.text-input
                 {:position "relative"}
                 [:input {:background-color (get-in COLORS [:grey :lightest])
                          :border "1px solid transparent"
                          :border-radius "4px"
                          :color (get-in COLORS [:grey :standard])
                          :height "40px"
                          :outline "none"
                          :padding (str "0 " (+ 16 (if right-icon 32)) "px 0 " (+ 16 (if left-icon 32)) "px")
                          :width "100%"}
                  [:&:focus {:border (str "1px solid " (:secondary COLORS))}]
                  [:&.error {:border (str "1px soild " (:error COLORS))}]]])
              (elmalike/start-with
                [false false]
                (elmalike/latest
                  (:left-icon-$ sources)
                  (:right-icon-$ sources))))
     :dom-$
     (recurrent.drivers.dom/isolate-sink scope
       (elmalike/map
         (fn [[is-error? label left-icon right-icon placeholder value]]
           `[:div {:class "text-input"}
             ~(if label `[:label ~label])
             [:input {:class ~(if is-error? "error")
                      :placeholder ~placeholder
                      :value ~value}]])
         (elmalike/latest
           (:is-error?-$ sources)
           (:label-$ sources)
           (:left-icon-$ sources)
           (:right-icon-$ sources)
           (:placeholder-$ sources)
           value-$)))}))

(defn DropDown
  [sources]
  (let [scope (gensym)
        dom-source-$ (recurrent.drivers.dom/isolate-source scope (:dom-$ sources))
        value-$ (elmalike/map
                  (fn [e] (dommy/text (.-selectedTarget e)))
                  ((dom-source-$ ".drawer-item") "click"))
        is-open?-$ (elmalike/foldp not false 
                                   (elmalike/merge
                                     (((:dom-$ sources) ".value") "click")
                                     (((:dom-$ sources) ".drawer-item") "click")))]
    {:css-$ (elmalike/signal-of
              [:.drop-down {:font-size "12px"
                            :font-family "sans-serif"
                            :position "relative"
                            :overflow "visible"
                            :width "100%"}
               [:* {:user-select "none"}]
               [:.value {:background-color "white"
                         :border "1px solid transparent"
                         :border-radius "4px"
                         :box-shadow "0px 1px 4px rgba(0,0,0,.33)"
                         :color (get-in COLORS [:grey :standard])
                         :cursor "pointer"
                         :height "40px"
                         :line-height "40px"
                         :padding "0 16px"
                         :position "relative"
                         :width "100%"}
                [:&.open {:border "1px solid black"}]]
               [:.arrow {:color (get-in COLORS [:grey :light])
                         :float :right
                         :font-size "10px"
                         :font-weight "bold"
                         :transform "rotateZ(-90deg)"}]
               [:.drawer {:background-color "white"
                          :border-bottom "1px solid black"
                          :border-left "1px solid black"
                          :border-right "1px solid black"
                          :border-bottom-left-radius "4px"
                          :border-bottom-right-radius "4px"
                          :color (get-in COLORS [:grey :light])
                          :clear "both"
                          :position "absolute"
                          :pointer-events "none"
                          :opacity 0
                          :top "52px"
                          :transition "opacity 250ms, top 250ms"
                          :width "100%"
                          :z-index 10}
                [:&.open {:top "36px"
                          :opacity 1
                          :pointer-events "all"}]]

               [:.drawer-item {:cursor "pointer"
                               :padding "12px 16px"
                               :user-select "none"}
                [:&:hover {:background "black"
                           :color "white"}]]])
     :dom-$
     (elmalike/map
       (fn [[items no-selection-value is-open? value]]
         `[:div {:class "drop-down"}
           [:div {:class ~(str "value " (if is-open? "open"))}
            ~(or value no-selection-value)
            [:div {:class "arrow"} "< >"]]
           [:div {:class ~(str "drawer " (if is-open? "open"))}
            ~(map (fn [item] `[:div {:class "drawer-item"} ~item]) items)]])
       (elmalike/start-with
         [nil nil nil nil]
         (elmalike/latest
           (:items-$ sources)
           (:no-selection-value-$ sources)
           is-open?-$
           value-$)))}))

(defn RangeInput
  [props sources]
  (let [scope (gensym)
        dom-source-$ (recurrent.drivers.dom/isolate-source scope (:dom-$ sources))
        mouse-x-$
        (elmalike/map
          (fn [e] 
            (println "x: " (.-offsetX e))
            (.-offsetX e))
          ((dom-source-$ ".range-input") "mousemove"))
        circle-x-$ (elmalike/start-with 0
                                        (elmalike/sample-between
                                          mouse-x-$
                                          ((dom-source-$ ".range-input") "mousedown")
                                          elmalike.mouse/mouseup-events))]

    {:css-$
     (elmalike/map
       (fn [circle-x]
         [:.range-input {:position "relative"
                         :height "32px"
                         :width "100%"}
          [:.circle {:background (get-in COLORS [:grey :lightest])
                     :box-shadow "0px 1px 4px rgba(0,0,0,.33)"
                     :pointer-events "none"
                     :position :absolute
                     :top 0
                     :left (str (- circle-x 16) "px")
                     :width "32px"
                     :height "32px"
                     :border-radius "16px"}]])
       circle-x-$)

     :dom-$
     (elmalike/map
       (fn [circle-x]
         `[:div {:class "range-input"}
           [:svg/svg {:width "100%" :height "100%"}
            [:svg/rect {:fill ~(get-in COLORS [:grey :light])
                        :rx "8" :ry "8" :x 0 :y "25%" 
                        :width "100%" :height "50%"}]
            [:svg/rect {:fill ~(:secondary COLORS)
                        :rx "8" :ry "8" :x "0" :y "25%" 
                        :width ~(str circle-x) :height "50%"}]]
           [:div {:class "circle"}]])
       circle-x-$)}))

(defn SwitchToggle
  [sources]
  (let [scope (gensym)
        dom-source-$ (recurrent.drivers.dom/isolate-source scope (:dom-$ sources))
        on?-$ (elmalike/foldp not false ((dom-source-$ ".switch-toggle") "click"))]
    {:css-$ (elmalike/map
              (fn [on?]
                [:.switch-toggle {:position "relative"
                                  :height "32px"
                                  :width "48px"}
                 [:.circle {:background (get-in COLORS [:grey :lightest])
                            :box-shadow "0px 1px 4px rgba(0,0,0,.33)"
                            :pointer-events "none"
                            :position :absolute
                            :top 0
                            :left (if on? "16px" 0)
                            :width "32px"
                            :height "32px"
                            :border-radius "16px"
                            :transition "left 250ms"}]])
              on?-$)

     :dom-$ (elmalike/map
              (fn [on?]
                `[:div {:class "switch-toggle"}
                  [:svg/svg {:width "100%" :height "100%"}
                   [:svg/rect {:fill ~(get-in COLORS (if on?
                                                       [:secondary]
                                                       [:grey :light]))
                               :rx "8" :ry "8" :x 0 :y "25%" 
                               :width "100%" :height "50%"}]]
                  [:div {:class "circle"}]])
              on?-$)}))


(defn RadioButton
  [sources])

(defn RadioGroup
  [sources])

(defn Main
  [sources]
  (let [regular-text-input (TextInput {:css-$ (:css-$ sources)
                                       :dom-$ (:dom-$ sources)})
        drop-down (DropDown {:no-selection-value-$ (elmalike/signal-of "No Selection")
                             :items-$ (elmalike/signal-of ["One" "Two" "Three"])
                             :css-$ (:css-$ sources)
                             :dom-$ (:dom-$ sources)})
        button (Button {:primary? true}
                       {:dom-$ (:dom-$ sources)
                        :text-$ (:value-$ regular-text-input)})
        range-input (RangeInput {} {:dom-$ (:dom-$ sources)
                                    :css-$ (:css-$ sources)})
        switch-toggle (SwitchToggle {:dom-$ (:dom-$ sources)
                                     :css-$ (:css-$ sources)})]

    (elmalike/subscribe-next (:dom-$ regular-text-input) println)

    {:css-$ (recurrent.drivers.css/collect :css-$
              regular-text-input
              drop-down
              button
              range-input
              switch-toggle)
     :dom-$ (recurrent.drivers.dom/collect :dom-$ 
              regular-text-input
              drop-down
              button
              range-input
              switch-toggle)}))


(defn main
  []
  (recurrent/run!
    Main
    {:css-$ (recurrent.drivers.css/from-id "style")
     :dom-$ (recurrent.drivers.dom/from-id "app")}))
