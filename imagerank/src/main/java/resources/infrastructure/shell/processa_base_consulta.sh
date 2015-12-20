#!/bin/bash

# valida imagem para processamento
imagemProcessamento=$1
if [ -z ${imagemProcessamento} ] || [ ! -f ${imagemProcessamento} ]; then

	echo "Imagem de processamento não informada ou não existente. Favor verificar.";
	exit 1;

elif [ ${imagemProcessamento##*.} != "jpg" ]; then

	echo "Imagem de processamento inválida. A imagem deve ter a extensão '.jpg'. Favor verificar.";
	exit 1;

fi

# parametros para rodar o processo online
ROOT_DIR="/home/imagerank/data/imagerank"
diretorioBaseConsultaPadrao="${ROOT_DIR}/base_consulta_padrao"
diretorioBaseProcessamentoConsulta="${ROOT_DIR}/processamento_consulta/"`date "+%Y%m%d"` # o diretorio base para processamento contem o ano, mes e dia
diretorioBaseCompleta="${ROOT_DIR}/base_completa"
diretorioBaseConsulta="${diretorioBaseProcessamentoConsulta}/"`basename ${imagemProcessamento} .jpg`  # gera sempre o diretorio contendo o nome da imagem
arquivoLog="${diretorioBaseConsulta}/processa_base_consulta.log"
diretorioImagens="imagens"
diretorioDescritores="descritores"
diretorioPares="pares"
diretorioLACDataset="lac_dataset"
diretorioExecucaoLAC="${ROOT_DIR}/bin/lac"
diretorioSaidaLAC="${diretorioBaseConsulta}/lac_output"
diretorioSaidaScore="score_output"
totalParesGerar=4000

# clona a base padrao de consulta para a base de processamento
mkdir -m 777 -p ${diretorioBaseProcessamentoConsulta} # cria o diretorio base, caso nao exista
rm -fr ${diretorioBaseConsulta} # remove o diretorio da consulta, para permitir n consultas na mesma data
cp -fr ${diretorioBaseConsultaPadrao} ${diretorioBaseConsulta}

# copia a imagem para processamento da consulta
cp ${imagemProcessamento} "${diretorioBaseConsulta}/${diretorioImagens}/"

# roda o processo online
echo "Acompanhe o log do arquivo '"$arquivoLog"'..."
java -Dfile.encoding=UTF-8 -cp ${ROOT_DIR}/imagerank-1.0.0-SNAPSHOT.jar br.ufmg.dcc.imagerank.main.ImageRankOnlineQueryProcessor $diretorioBaseCompleta $diretorioBaseConsulta $diretorioImagens $diretorioDescritores $diretorioPares $diretorioLACDataset $diretorioExecucaoLAC $diretorioSaidaLAC $diretorioSaidaScore $totalParesGerar 2> $arquivoLog
