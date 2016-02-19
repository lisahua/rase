# RASE

When developers add features and fix bugs, they often make systematic edits—similar edits to multiple locations. Systematic edits may indicate that developers should instead refactor to eliminate redundancy. This paper explores this question by designing and implementing a fully automated refactoring tool called RASE, which performs clone removal. 

RASE (1) extracts common code guided by a systematic edit; (2) creates new types and methods as needed; (3) parameterizes differences in types, methods, variables, and expressions; and (4) inserts return objects and exit labels based on control and data flow. To our knowledge, this functionality makes RASE the most advanced refactoring tool for automated clone removal.


We evaluate RASE with real-world systematic edits and com- pare to method based clone removal. RASE successfully performs clone removal in 30 of 56 method pairs (n=2) and 20 of 30 method groups (n≥3) with systematic edits. We find that scoping refactoring based on systematic edits (58%), rather than the entire method (33%), increases the applicability of automated clone removal. Automated refactoring is not feasible in the other 42% cases, which indicates that automated refactoring does not obviate the need for systematic editing.
