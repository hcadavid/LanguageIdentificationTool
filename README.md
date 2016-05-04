##Text language identifier
###based on Cavnar and Trenkle paper[1].
Author: Héctor Fabio Cadavid R.

This software consists on two tools: the NGrams statistics database generator (1), and the language 
identifier for documents (2). Both tools provides information about the required parameters. 

(1) java nlp.dipftest.NGramExtractor
(2) java nlp.dipftest.LangIdentificationException

There are two pre-built databases included in the db/ folder:

* ngramsBd-EN_ES_DE_FR.sqlite: 	n-grams extracted from European Parliament Proceedings Parallel Corpus 1996-2011
								in English, Spanish, Deutsch, and French.

* ngramsBd.sqlite:	n-grams extracted from a corpus generated from wikipedia (Spanish), and the
								gold-standard corpus used in morphochallenge (English).


[1] W. B. Cavnar and J. M. Trenkle, "N-Gram-Based Text Categorization,
"Proceedings of the 1994 Sym- posium on Document Analysis and Information Retrieval
(Univ.of Nevada, Las Vegas, 1994), p. 161.
