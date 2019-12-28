(ns clj-photo-org.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-photo-org.core :refer :all]))


(deftest get-full-path-files-in-dir-test
    (let [test-data (str (class (first (get-full-path-files-in-dir "resources/test_input"))))]
      (= (is "class java.io.File" test-data))))


(deftest files-to-process-test
  (let [test-data (-> (files-to-process "resources/test_input/")
                      first ; this is LazySeq so must "get it"
                      (str/split #"/")
                      last)] ; get file name
   (= (is "sample.JPG" test-data))))


(deftest get-photo-date-taken-test
  (let [test-data (get-photo-date-taken "resources/test_input/sample.JPG")]
    (= (is "2018:03:22 12:30:22" test-data))))


(deftest make-date-object-test
  (let [test-data (get-photo-date-taken "resources/test_input/sample.JPG")
        date-object (->> (make-date-object test-data)
                         class
                         str)]
    (is (= date-object "class java.time.LocalDateTime"))))


(deftest stringify-single-digit-test
  (let [test-data1 (stringify-single-digit 1)
        test-data2 (stringify-single-digit 10)]
    (is (= test-data1 "01"))
    (is (= test-data2 "10"))))


(deftest replace-colon-with-dash-test
  (let [test-data (replace-colon-with-dash "1:2:3")]
    (is (= test-data "1-2-3"))))


(deftest check-date-format-test
  (let [test-data1 (check-date-format "2019-12-24T17-52")
        test-data2 (check-date-format "2019-12-24T17-52-27")]
    (is (= test-data1 "2019-12-24T17-52-00"))
    (is (= test-data2 "2019-12-24T17-52-27"))))


(deftest make-photo-map-test
  (let [test-data (make-photo-map "resources/test_input/sample.JPG")
        sample-output {:month-name "MARCH",
                       :date-time-as-string "2018-03-22T12-30-22",
                       :file-md5-sum "1b7a4cb",
                       :month 3,
                       :day-of-month 22,
                       :year 2018,
                       :month-as-string "03",
                       :file-path "resources/test_input/sample.JPG",
                       :weekday "THURSDAY",
                       :target-name "2018-03-22T12-30-22-1b7a4cb.jpg"}]
    (is (= test-data sample-output))))


(deftest process-one-element-test
  (let [del-output-dir (delete-directory-recursive (io/as-file "resources/test_output/"))
        create-output-dir (.mkdir (io/as-file "resources/test_output/"))
        test-element (make-photo-map "resources/test_input/sample.JPG")
        do-process (process-one-element test-element "resources/test_output/")
        result-file (-> (files-to-process "resources/test_output/")
                        first ; this is LazySeq so must "get it"
                        (str/split #"/")
                        last)]
    (is (= result-file "2018-03-22T12-30-22-1b7a4cb.jpg"))))
