
dbload:
	javac constants.java dbload.java

dbquery:
	javac constants.java dbquery.java

btindex:
	javac btindex.java btsearch.java

compileAll: dbload dbquery btindex

index:
	java dbload -p 4096 artist.trim.csv

birthIndex:
	java btindex 4096 heap.4096

query:
	java dbquery 4096 19700101 19701231

indexQuery:
	java btsearch heap.4096 index.4096 19700101 19701231


test1:
	@# Pagesize 4096, between dates 19700101 19701231
	@ echo "Running test 1."
	@ echo "Building heap & index."
	@ java dbload -p 4096 artist.trim.csv > temp.txt
	@ java btindex 4096 heap.4096 > temp.txt
	@ echo "Running query."
	@ java dbquery 4096 19700101 19701231 > dbqueryResult.txt
	@ java btsearch heap.4096 index.4096 19700101 19701231 > btindexResult.txt
	@ rm -rf temp.txt
	@ echo "Test1" >> testResults.txt
	@ diff dbqueryResult.txt btindexResult.txt >> testResults.txt || true

test2:
	@# Pagesize 8192, between dates 19700101 19701231
	@ echo "Running test 2."
	@ echo "Building heap & index."
	@ java dbload -p 8192 artist.trim.csv > temp.txt
	@ java btindex 8192 heap.8192 > temp.txt
	@ echo "Running query."
	@ java dbquery 8192 19700101 19701231 > dbqueryResult.txt
	@ java btsearch heap.8192 index.8192 19700101 19701231 > btindexResult.txt
	@ rm -rf temp.txt
	@ echo "Test1" >> testResults.txt
	@ diff dbqueryResult.txt btindexResult.txt >> testResults.txt || true

test3:
	@# Pagesize 4096, between dates 19730101 19741231
	@ echo "Running test 3."
	@ echo "Building heap & index."
	@ java dbload -p 4096 artist.trim.csv > temp.txt
	@ java btindex 4096 heap.4096 > temp.txt
	@ echo "Running query."
	@ java dbquery 4096 19730101 19741231 > dbqueryResult.txt
	@ java btsearch heap.4096 index.4096 19730101 19741231 > btindexResult.txt
	@ rm -rf temp.txt
	@ echo "Test3" >> testResults.txt
	@ diff dbqueryResult.txt btindexResult.txt >> testResults.txt || true

test4:
	@# Pagesize 4096, between dates 19000101 20220101
	@ echo "Running test 4."
	@ echo "Building heap & index."
	@ java dbload -p 4096 artist.trim.csv > temp.txt
	@ java btindex 4096 heap.4096 > temp.txt
	@ echo "Running query."
	@ java dbquery 4096 19000101 20220101 > dbqueryResult.txt
	@ java btsearch heap.4096 index.4096 19000101 20220101 > btindexResult.txt
	@ rm -rf temp.txt
	@ echo "Test4" >> testResults.txt
	@ diff dbqueryResult.txt btindexResult.txt >> testResults.txt || true

test5:
	@# Pagesize 4096, between dates 19700101 19700102
	@ echo "Running test 5."
	@ echo "Building heap & index."
	@ java dbload -p 4096 artist.trim.csv > temp.txt
	@ java btindex 4096 heap.4096 > temp.txt
	@ echo "Running query."
	@ java dbquery 4096 19700101 19700102 > dbqueryResult.txt
	@ java btsearch heap.4096 index.4096 19700101 19700102 > btindexResult.txt
	@ rm -rf temp.txt
	@ echo "Test5" >> testResults.txt
	@ diff dbqueryResult.txt btindexResult.txt >> testResults.txt || true

testall: clean compileAll
	@# Tests write output to testResults.txt
	@ echo "Running tests, may take some time."
	@ make test1
	@ make test2
	@ make test3
	@ make test4
	@ make test5
	@ echo "Tests saved to testResultst.txt"
	@ echo "Test results:"
	@ cat testResults.txt
	


zip:
	mkdir -p a1-sample-solution
	cp *.java fixdates.py Makefile a1-sample-solution
	zip -r a1-sample-solution a1-sample-solution
	rm -fr a1-sample-solution


clean:
	$(RM) *.class heap.* index.* temp.txt
	rm -fr testResults.txt
