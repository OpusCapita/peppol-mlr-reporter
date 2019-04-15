# peppol-mlr-reporter [![CircleCI](https://circleci.com/gh/OpusCapita/peppol-mlr-reporter.svg?style=svg)](https://circleci.com/gh/OpusCapita/peppol-mlr-reporter)

Peppol OpusCapita Access Point mlr-reporter service running on Andariel Platform.

The service reads files from the `peppol.mlr-reporter.queue.in.name` queue and processes them. The processing includes:

* Creating MLR structure
* Sending MLR file to related business platform

Please check the wiki pages for more information:
* [Preprocessing](https://opuscapita.atlassian.net/wiki/spaces/IIPEP/pages/107806873/New+Peppol+solution+modules+description#NewPeppolsolutionmodulesdescription-preprocessing)
* [Internal Routing](https://opuscapita.atlassian.net/wiki/spaces/IIPEP/pages/107806873/New+Peppol+solution+modules+description#NewPeppolsolutionmodulesdescription-internal-routing)