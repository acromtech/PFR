recherche : tst_RGB.c indexationImage.c indexationImage.h
	gcc -c tst_RGB.c
	gcc -c indexationImage.c
	gcc -c comp_image.c
	gcc -o test.out tst_RGB.o comp_image.o indexationImage.o -g
	./test.out TXT


clean :
	rm -f *.o*
	echo > base_descripteur_image.csv
	echo > liste_descripteur_image.csv
