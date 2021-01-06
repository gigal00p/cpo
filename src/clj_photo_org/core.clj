(ns clj-photo-org.core
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]
   [digest]
   [eftest.runner :refer [find-tests run-tests]]
   [java-time :as time]
   [exif-processor.core :as exf]

   [taoensso.timbre :as timbre :refer [info  warn  error]]))
(refer-clojure :exclude [range iterate format max min])


(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn read-exif-photo-date-taken
  "TODO docstring"
  [photo-file-path]
  (let [input-file (io/as-file photo-file-path)
        all-metadata (exf/exif-for-file input-file)]
    (get all-metadata "Date/Time")))


(defn has-exif-data?
  [file-path]
  (if (string? (read-exif-photo-date-taken file-path))
    true
    false))


(defn get-full-path-files-in-dir
  "Returns LazySeq of java.io.File objects of the passed directory.
   `:recursively true/false` keyword controls if fs walk will be done recursively"
  [path & {:keys [recursively]}]
  (let [file (io/file path)
        coll (if (.isDirectory file)
                 (if (= true recursively)
                   (file-seq file)
                   (->> file
                        .listFiles))
               (error (str "Passed path: `" path "` is not a directory"))
                   ; (exit -1 "Cannot proceed")
                   )] ; exit if passed dir string does not exist
    (if (empty? coll)
      [] ; retrn empty vector if directory does not contain any elements, otherwise return results
      (remove #(.isDirectory %) coll))))


(defn files-to-process
  "Check if there are any files to process"
  [dir]
  (let [all-files (get-full-path-files-in-dir dir :recursively true)
        filters [#(.endsWith (.toLowerCase %) ".jpg") #(.endsWith (.toLowerCase %) ".jpeg")]
        potential-files-to-process (if (empty? all-files)
                                     ()
                                     (->> (map #(.getAbsolutePath %) all-files)))
        valid-files-to-process  (->> (map #(filter % potential-files-to-process) filters)
                                     flatten)]
    (if (empty? valid-files-to-process)
      (error (str "No files to process in the directory" dir))
      (let [all-files (group-by has-exif-data? valid-files-to-process)
            files-with-exif (get all-files true)
            files-without-exif (get all-files false)]
        {:files-with-exif files-with-exif
         :files-without-exif files-without-exif}))))

(defn make-date-object
  [string-date]
  (time/local-date-time "yyyy:MM:dd HH:mm:ss" string-date))


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


(defn calculate-md5-substring-of-file
  "Calculate md5 sum of file. Return string of first 7 characters
  of the checksum if the file exists, nil otherwise."
  [file-path]
  (let [file (io/as-file file-path)]
    (if (.exists file)
      (subs (digest/md5 file) 0 7)
      (do
        (warn "File" file-path "does not exists" )
        nil))))


(defn make-photo-map
  [photo]
  (let [date-object (make-date-object (read-exif-photo-date-taken photo))
        day-of-month (.getDayOfMonth date-object)
        weekday (.toString (.getDayOfWeek date-object))
        month (.getMonthValue date-object)
        month-as-string (stringify-single-digit month)
        month-name (.toString (.getMonth date-object))
        year (.getYear date-object)
        date-time-as-string (check-date-format (replace-colon-with-dash (.toString date-object)))
        md5-sum (calculate-md5-substring-of-file photo)
        target-name (str date-time-as-string "-" md5-sum ".jpg")]
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


(defn copy-file
  [source-path dest-path]
  (io/copy (io/file source-path) (io/file dest-path)))


(defn process-one-element
  [element target-root-directory]
  (let [source-path (:file-path element)
        dest-year (str (:year element))
        dest-month-number (:month-as-string element)
        dest-month-name (:month-name element)
        dest-path (str target-root-directory "/" dest-year "/" dest-month-number "-" dest-month-name "/" (:target-name element))
        _ (io/make-parents dest-path)] ; prepare directory tree for target file
    (info "Copying file" source-path "to" dest-path)
    (copy-file source-path dest-path)))


(defn process-files
  [coll target-directory]
  (let [number-processed-files (->> (map #(process-one-element % target-directory) coll)
                                    (into [])
                                    count)
        msg (info "Succesfully processed" number-processed-files "files")]))


(defn delete-directory-recursive
  "Recursively delete a directory."
  [^java.io.File file]
  ;; when `file` is a directory, list its entries and call this
  ;; function with each entry. can't `recur` here as it's not a tail
  ;; position, sadly. could cause a stack overflow for many entries?
  (when (.isDirectory file)
    (doseq [file-in-dir (.listFiles file)]
      (delete-directory-recursive file-in-dir)))
  ;; delete the file or directory. if it it's a file, it's easily
  ;; deletable. if it's a directory, we already have deleted all its
  ;; contents with the code above (remember?)
  (io/delete-file file))


(def cli-options
  [["-i" "--input DIR" "Directory that contains JPG files (can be nested dir structure)"]
   ["-o" "--output DIR" "Where renamed and organized JPG files will be written"]
   ["-h" "--help"]])


(defn help [options]
  (->> ["clj-photo-org is a command line tool for organizing collection of jpg files into directory structure."
        ""
        "Usage: java -jar clj-photo-org-0.1.0-standalone.jar [options]"
        ""
        "Options:"
        options
        ""]
       (str/join \newline)))


(defn process-single-bad-file
  [target-path file]
  (let [source-path (->> (io/as-file file) .getAbsolutePath)
        md5-sum (calculate-md5-substring-of-file file)
        file-name-with-extension (->> (io/as-file file)
                                      .getName)
        file-name-without-extension (first (str/split file-name-with-extension #"\."))
        dest-path (str target-path "/" "NO_EXIF_DATA_FILES" "/" file-name-without-extension "-" md5-sum ".jpg")
        _ (io/make-parents dest-path)] ; prepare directory tree for target file
    (warn "File" file-name-with-extension "does not contain EXIF meatadata and will be copied to" dest-path)
    (copy-file source-path dest-path)))


(defn -main [& args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (help summary))
      (not= (count options) 2) (exit 0 (str "Not enough options provided, usage:\n\n" (help summary)))
      (not= (count errors) 0) (exit 0 (str "CLI arguments parsing failed, usage:\n\n" (help summary)))
      :else
      (try
        (let [input-dir (->> options :input)
              output-dir (->> options :output)
              all-files (files-to-process input-dir)
              parsed-photos-with-exif (->> (:files-with-exif all-files)
                                           (pmap #(make-photo-map %)))
              parsed-photos-without-exif (:files-without-exif all-files)
              no-of-good (count parsed-photos-with-exif)
              no-of-bad (count parsed-photos-without-exif)]

            (if (> no-of-good 0)
              (process-files parsed-photos-with-exif output-dir)
              "No files to process found")

            (if (> no-of-bad 0)
              (doall (map #(process-single-bad-file output-dir %) parsed-photos-without-exif))
                  (info "Couldn't parse" no-of-bad "files")))

        (exit 0 "Program finished.")

        (catch Exception e
          (timbre/errorf "Something went wrong: %s" (.getMessage ^Exception e)))))))

(comment (run-tests (find-tests "test")))
