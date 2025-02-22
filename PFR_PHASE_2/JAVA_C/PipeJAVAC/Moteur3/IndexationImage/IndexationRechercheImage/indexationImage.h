#ifndef INDEXATION_IMAGE_IN
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define NB_BITS 2
#define NBLISTE 10
#define TAILLE_MALLOC 1024

#define min(a,b)(((a)<(b))?(a):(b))        //Macro permettant de déterminer le minimum entre deux valeurs a et b

typedef enum {N ='N',c ='c'}type;

typedef struct descripteur_image{
    int id;
    int *histogramme;
    int taille;
    char type;
    struct descripteur_image *next;
}descImage;

typedef descImage* descripteurImage;

typedef struct basedescripteur{
    int taillle;
    descripteurImage tete;
}*baseDescripteurImage;

baseDescripteurImage initBaseDescripteurImage();
void empilerBaseDescripteurImage(baseDescripteurImage*, struct descripteur_image);
void depilerBaseDescripteurImage(baseDescripteurImage*, struct descripteur_image*);
char* descripteurImageToString(struct descripteur_image);

typedef struct elementlistedescripteurimage{
    int id;
    char *path;
    struct elementlistedescripteurimage *next;
}*elementlitsetDescripteurImage;

typedef struct listedescripteurimage{
    int taille;
    struct elementlistedescripteurimage * tete;
}*listeDescripteurImage;
listeDescripteurImage initListeDescripteurImage();

int* quantification(char *, int, int*);
void saveDescripteurImage(baseDescripteurImage*,FILE*, struct descripteur_image);
void savelisteDescripteurImage(listeDescripteurImage*, FILE*, char*, int id);
void indexationImage(char*, baseDescripteurImage*, listeDescripteurImage*, int, int);
void indexerBaseImage(baseDescripteurImage *bd, listeDescripteurImage *listeDescripteur);
void supprimerBaseImage();
void indexerBaseImageMenu();

typedef struct score{
    float score;
    char type;
    int id;
    struct score* next;
}ScoreImage;

typedef ScoreImage* Src;

typedef struct base_score{
    int taille;
    Src tete;
}*baseScore;

typedef struct descripteurEtScoreImage{
    struct elementlistedescripteurimage *descripteur;
    int score;
    struct descripteurEtScoreImage* next;
}*descripteurEtScoreListeImage;

descripteurEtScoreListeImage rechercheCouleur(const volatile baseDescripteurImage, listeDescripteurImage , char *);
descripteurEtScoreListeImage empilerDescripteurEtScoreImage(descripteurEtScoreListeImage, char*, int, int);
Src calculeScoreCouleur(const volatile baseDescripteurImage,char*,int*);

descripteurEtScoreListeImage rechercheHisto(const volatile baseDescripteurImage, listeDescripteurImage, char *);
Src calculeScoreComparaison(const volatile baseDescripteurImage,descImage,int*);

char* trouveCheminImage(int,listeDescripteurImage);
int trouveIDDescripteurImage(char*,listeDescripteurImage,int*);

int afficheResultatsRecherche(Src,int,listeDescripteurImage);
ScoreImage choixFichierImage(Src,int);
int ouvreFichierImage(ScoreImage,listeDescripteurImage);
void rechercheImageHisto();
void rechercheImageCouleur();

char* getNomFichierImage(char*);
int compareScore(const void*,const void*);

// Partie JAVA

char *trouverCheminImage(int, listeDescripteurImage);
void cleanPath(char* new_path, char *path);
void insertionSortImage(Src tab, int size);

#endif // !INDEXATION_IMAGE_IN