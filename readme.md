# Spring Data JPA Learnings

## Hibernate Interceptor

* Whenever Book Entity is Inserted, Modified or Deleted 
  * Make an entry at History table table as well with changes
* A generic library is written
  * `AuditHistory` annotation at entity
  * `AuditHistoryIgnore` annotation to ignore a property
    * Can be multiple
  * `AuditHistoryIdentifier` annotaiton for Identifier Method for *new* and *delete* operations