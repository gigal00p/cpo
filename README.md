# clj-photo-org [![CircleCI](https://circleci.com/gh/gigal00p/cpo.svg?style=shield)](https://app.circleci.com/pipelines/github/gigal00p/cpo)

`cpo` is simple CLI tool that renames JPG photos and organizes them into `YYYY/MM-MONTH-NAME`
directory structure.

Suppose you have a bunch of JPG files nested under some directory. These might be from
different cameras and therefore there might be name collision (e.g. two different files
named IMG_4613.JPG. This small program will rename them based on their exif info and append
part of md5 checksum string to each file name.

After rename operation it will organize them into folder structure similar to:

```.
└── 2018
    └── 03-MARCH
    └── 2018-03-22T12-30-22-1b7a4cb.jpg
```

Original files are preserved. No changes are made to output files other than rename and `mv` into
desired dir structure.

## Installation

Download latest fat jar from this repo [releases](https://github.com/gigal00p/cpo/releases) or compile it using leiningen:

1. Install [Leiningen](https://leiningen.org/)
2. `git clone https://github.com/gigal00p/cpo`
3. cd `cpo`
4. run `lein uberjar`
5. Under target/uberjar you'll find standalone jar to run

## Usage

    $ java -jar clj-photo-org-0.1.0-standalone.jar [args]

## Options

* `-i input path` - might contain nested directories with `JPG` files.
* `-o output_path` - directory where organized structure will be created.

## Examples

`java -jar clj-photo-org-0.1.0-SNAPSHOT-standalone.jar -i /home/username/pictures/sample -o /home/username/temp/results/`

REPL invocation: `(-main "-i" "c:/Users/username/Downloads/input" "-o" "c:/Users/username/Downloads/output/")`

### Bugs

Via github.

## License

Copyright © 2021 gigal00p

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
