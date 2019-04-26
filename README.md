# peppol-mlr-reporter [![CircleCI](https://circleci.com/gh/OpusCapita/peppol-mlr-reporter.svg?style=svg)](https://circleci.com/gh/OpusCapita/peppol-mlr-reporter)

Peppol OpusCapita Access Point mlr-reporter service running on Andariel Platform.

The service reads files from the `peppol.mlr-reporter.queue.in.name` queue and processes them. The processing includes:

* Creating MLR structure
* Sending MLR file to related business platform

Please check the wiki pages for more information.