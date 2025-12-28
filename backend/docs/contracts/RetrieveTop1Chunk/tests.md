RetrieveTop1ChunkByQueryUseCaseImpl – Tests v1

| Must ID | 测试方法名 | 状态 |
| --- | --- | --- |
| M1 | givenNullOrBlankQueryWhenExecuteThenInvalidQuery | DONE |
| M2 | givenTooLongQueryWhenExecuteThenInvalidQuery | DONE |
| M3 | givenQueryWithSurroundingSpacesWhenExecuteThenUsesTrimmedQuery | DONE |
| M4 | givenEmbeddingTimeoutWhenExecuteThenEmbeddingTimeout | DONE |
| M5 | givenVectorSearchTimeoutWhenExecuteThenVectorSearchTimeout | DONE |
| M6 | givenTotalTimeoutWhenExecuteThenTotalTimeout | DONE |
| M7 | givenValidQueryWhenSearchThenReturnTop1AndUseTopKOne | DONE（组件）；TODO（集成） |
| M8 | givenVectorReturnsEmptyWhenExecuteThenNotFound | DONE |
| M9 | givenFailureWhenExecuteThenNoImplicitRetry | DONE |

