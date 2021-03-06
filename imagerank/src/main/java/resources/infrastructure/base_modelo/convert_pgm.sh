imagensInputPath="imagens"
imagensPgmOutputPath="imagens_pgm/imagens/"

if [ -d $imagensPgmOutputPath ]; then

	rm -r $imagensPgmOutputPath
fi


for file in `find $imagensInputPath -name *.jpg`
do
	#convert image to pgm
	inputImageName=$file
	fileDirOutput=`dirname $file`
	fileDirOutput=`expr "$fileDirOutput" : '[^/]*/\(.*\)'` # remove o primeiro diretorio da imagem
	fileOutput=`basename $file .jpg`".pgm" # gera o novo arquivo com nome da imagem, trocando sua extensao para .pgm
	directoryOutput=${imagensPgmOutputPath}${fileDirOutput}
	outputImageName=${directoryOutput}"/"${fileOutput}

	mkdir -p ${directoryOutput} # garante que a estrutura de diretorios/subdiretorios exista
	convert -compress none $inputImageName $outputImageName # converte a imagem original em pgm
	sed -i '/#CREATOR/d' $outputImageName # tira comentarios do pgm
done

