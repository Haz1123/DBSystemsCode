
dbload:
	javac constants.java dbload.java

dbquery:
	javac constants.java dbquery.java

btindex:
	javac btindex.java btsearch.java

index:
	java dbload -p 4096 artist.trim.csv

query:
	java dbquery 4096 19700101 19701231

birthIndex:
	java btindex 4096 heap.4096



zip:
	mkdir -p a1-sample-solution
	cp *.java fixdates.py Makefile a1-sample-solution
	zip -r a1-sample-solution a1-sample-solution
	rm -fr a1-sample-solution

clean:
	$(RM) *.class heap.4096
