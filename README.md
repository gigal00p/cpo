# clj-photo-org

`cpo` is simple CLI tool that renames JPG photos and organizes them into `YYYY/MM-MONTH-NAME`
directory structure.

Suppose you have a bunch of JPG files nested under some directory. These might be from
different cameras and therefore there might be name collision (e.g. two different files
named IMG_4613.JPG. This small program will rename them based on their exif info and append
md5 checksum of.

After rename operation it will organize them into folder structure similar to:
`.
└── 2018
    └── 03-MARCH
        └── 2018-03-22T12-30-22-1b7a4cb.jpg`

## Installation

[Leiningen](https://leiningen.org/) tool must be installed before using this tool.

1. `git clone https://github.com/gigal00p/cpo`
2. cd `cpo`
3. run `lein uberjar`
4. Under target/uberjar you'll find standalone jar to run

## Usage

    $ java -jar clj-photo-org-0.1.0-standalone.jar [args]

## Options

* `-i input path`
* `-o output_path`

## Examples

`java -jar clj-photo-org-0.1.0-SNAPSHOT-standalone.jar -i /home/username/pictures/sample -o /home/username/temp/results/`


### Bugs

Via github.

## License

Copyright © 2019 gigal00p

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
