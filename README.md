# PFR-audio 

Dernière update : 27/01/2023

Auteur : **Emilien VESIN et Bastien LALANNE**

module : Informatique

## Information

Cette partie a consisté à réaliser l'indexation d'une base de fichiers audio au format .bin et faire la comparaison de fichiers audios. Cette partie a été réalisé de façon collaborative par Emilien VESIN et Bastien LALANNE.

## Prérequis
* Système d'exploitation : Linux

* Installer VLC

## Utilisation

__1 Indexation Audio

Pour lancer l'indexation audio, il faut ouvrir un terminal et ce placer dans le dossier IndexationAudio puis faire un make. Cela va lancer le fichier tstIndexationAudio, qui va supprimer la BaseDescripteursAudio et la ListeDescripteursAudio, cela va ensuite reindexer la base de fichier contenu dans le dossier TestSon avec le nombre d'échantillon (n) et le nombre d'intervalle (m) contenu dans le .config (indexationAudioN, indexationAudioM). 

> ```
> 2 indexationAudioN 1024
> 3 indexationAudioM 100
> ```

__2 Configuration

Pour modifier les valeurs de ces variables on peut utiliser la fonction saisieConfig contenu dans le fichier configuration.c en se déplaçant dans le dossier Configuration et en faisant un make. Le fichier .config va s'afficher puis il faut entrer la ligne que l'on veut modifier et sa nouvelle valeur, la nouvelle configuration s'affiche, cela signifie que la nouvelle valeur a bien été pris en compte.  

> ```
> 2 indexationAudioN 1024
> 
> 3 indexationAudioM 100
> 
> 4 seuil 89
> 
> 5 dureeAudioCorpus 70
> 
> 
> Entrez le numéro du paramètre qui doit être modifié : 3
> 
> Entrez la nouvelle valeur pour le paramètre n° 3 : 50
> 
> 
> 2 indexationAudioN 1024
> 
> 3 indexationAudioM 50
> 
> 4 seuil 89
> 
> 5 dureeAudioCorpus 70
> ```

__3 Recherche Audio

Pour lancer la recherche audio, il faut ouvrir un terminal et ce placer dans le dossier RechercheAudio puis faire un make. Cela va lancer le fichier tstRechercheAudio, le système va ensuite vous demandez de renseigner le nom du fichier que vous voulez comparer (ex: jingle_fi.wav), il va ensuite afficher le pourcentage de correspondance ainsi que la position temporelle de la correspondance. Le système vous demandera d'appuyer sur la touche "Entrer" afin de lancer la lecture du fichier à la position de la correspodance.

> ```
>| Entrez le nom de votre fichier : jingle_fi.wav
>
>
> | Veuillez patienter | 
> 
> | Correspondance trouvé à 90.32% ... | 
> 
> | Voici le résultat | 
> 
> 
> Le jingle demarre à environ 29 sec du corpus
> 
> Appuyez sur entrée pour lancer l'audio... (3 sec avant)
> ```
