(ns clj-photo-org.core
  (:gen-class)
  (:require [taoensso.timbre :as timbre :refer [debug  info  warn  error  fatal]]
            [clojure.string :as str]))

(require 'digest)
(require '[clj-exif.core :as exif])
(require '[clojure.java.io :as io])
(refer-clojure :exclude [range iterate format max min])
(use 'java-time)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def photo "/home/krzysztof/Obrazy/sample/IMG_4813.JPG")

(def input-dir "/home/krzysztof/Obrazy/sample")
(def big-input-dir "/home/krzysztof/Obrazy/100CANON")

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn get-full-path-files-in-dir
  "Returns absolute path of files in the passed directory.
   :recursively? keyword controles weather walk will be done recursively"
  [path & {:keys [recursively]}]
  (let [file (io/file path)]
    (if (.isDirectory file)
      (do
        (if (= true recursively)
          (file-seq file)
          (->> file
               .listFiles)))
      (do (error (str "Passed path: `" path "` is not a directory"))
          ; (exit -1 "Cannot proceed")
          ))))


(defn files-to-process
  "Filter files to process"
  [dir]
  (let [coll (->> (get-full-path-files-in-dir dir)
                  (map #(.getAbsolutePath %)))
        filters [#(.endsWith % ".JPG") #(.endsWith % ".jpg")]]
     (->> (map #(filter % coll) filters) ; accept only JPG and jpg extensions
          flatten)))


(defn get-photo-date-taken
  ; TODO error handling
  [photo-file-path]
  (let [input-file (java.io.File. photo-file-path)
        metadata (exif/get-metadata input-file)
        photo-metadata (exif/read metadata)]
    (-> photo-metadata
        (get "Root")
        (get "DateTime"))))


(defn make-date-object
  [string-date]
  (local-date-time "yyyy:MM:dd HH:mm:ss" string-date))


(defn get-object-methods
  [myObject]
  (vec (.getMethods (.getClass myObject))))

(defn stringify-single-digit
  [month-number]
  (if (< month-number 10)
    (str "0" month-number)
    (str month-number)))

(defn replace-colon-with-dash
  [date-time-string]
  (str/replace date-time-string #":" "-"))

(defn check-date-format
  [string]
  (if (= (count string) 16)
    (str string "-00")
    string))
  

(defn make-photo-map
  [photo]
  (let [date-object (make-date-object (get-photo-date-taken photo))
        day-of-month (.getDayOfMonth date-object)
        weekday (.toString (.getDayOfWeek date-object))
        month (.getMonthValue date-object)
        month-as-string (stringify-single-digit month)
        month-name (.toString (.getMonth date-object))
        year (.getYear date-object)
        date-time-as-string (check-date-format (replace-colon-with-dash (.toString date-object)))
        md5-sum (subs (digest/md5 (io/as-file photo)) 0 7)
        target-name (str date-time-as-string "-" md5-sum ".jpg")]
    ; (info "Processing file" photo "- new filename:" target-name)
    {:day-of-month day-of-month
     :weekday weekday
     :month month
     :month-as-string month-as-string
     :month-name month-name
     :year year
     :date-time-as-string date-time-as-string
     :file-path photo
     :file-md5-sum md5-sum
     :target-name target-name})) 


(def target-directory "/home/krzysztof/temp/results/")


(defn copy-file
  [source-path dest-path]
  (io/copy (io/file source-path) (io/file dest-path)))


(defn process-one-element
  [element target-directory]
  (let [source-path (:file-path element)
        dest-path (str target-directory (:target-name element))
        prepare-target (io/make-parents dest-path)] ; prepare directory tree for target file
    ;(info "Processing file" dest-path)
    (copy-file source-path dest-path)))


; (def wszystkie-male (into [] (pmap #(make-photo-map %) (files-to-process input-dir))))
; (def wszystkie-duze (into [] (pmap #(make-photo-map %) (files-to-process big-input-dir))))

(defn process-files
  [coll]
  (map #(process-one-element % target-directory) coll))
    


