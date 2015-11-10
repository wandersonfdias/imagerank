#!/bin/bash
diretorioBase="$PWD/base_completa"
diretorioImagens="imagens"
diretorioDescritores="descritores"
diretorioPares = "pares";
diretorioLACDataset = "lac_dataset";
arquivoParesOriginal="$PWD/pares_originais/tag-classes-reduced.dat"
totalParesGerar = 600000;

java -cp imagerank-1.0.0-SNAPSHOT.jar br.ufmg.dcc.imagerank.main.ImageRankBatchProcessor $diretorioBase $diretorioImagens $diretorioDescritores $diretorioPares $diretorioLACDataset $arquivoParesOriginal $totalParesGerar