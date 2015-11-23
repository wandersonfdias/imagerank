/**
THIS MODULE IMPLEMENTS ALL OPERATIONS OF READING/PARSING THE TRAINING DATA.
*/

#include "training.h"

/**
READS THE TRAINING DATA. ALL ITEMS ARE STORED IN A GLOBAL ARRAY, WHICH IS
THEN ACCESSED DURING RULE INDUCTION.
*/
int read_training_set(char* training) {
	__START_TIMER__
	printf("Reading training data ");
	fflush(stdout);
	int target=-1;
	int hasCache=0;
	char** target_name=(char**)malloc(sizeof(char*)*MAX_CLASSES);
	for(int i=0;i<MAX_CLASSES;i++) target_name[i]=(char*)malloc(sizeof(char)*100);
	set<string> proc_items;
	FILE* file=fopen(training,"r");
	if(file==NULL) {
		fprintf(stderr,"Training set %s not found.\n\n", training);
		exit(-1);
	}
	free(training);

	/** // TODO Wanderson - comentado por nao estar sendo utilizado
	int n_lines=0;
	char done[10];
	while(1) {
		char line[200*KB];
		fgets(line, 200*KB, file);
		if(feof(file)) break;
		n_lines++;
	}
	rewind(file);
	*/

	if (CACHE_TRAINING_DATA==1) {

		struct stat st = {0};
		if (stat(TRAINING_CACHE_PATH, &st) == -1) {
		    mkdir(TRAINING_CACHE_PATH, 0777); // cria o diretorio para cache do treino
		}

		if (ONLY_PROCESS_TRAINING==0) {

			// VARIAVEIS AUXILIARES
			FILE* fp = fopen(VAR_AUX_FILE, "r");

			if (fp!=NULL) {

				// VARIAVEIS AUXILIARES
				fscanf( fp
					   , "N_TRANSACTIONS=%d N_ITEMSETS=%d"
					   , &N_TRANSACTIONS
					   , &N_ITEMSETS
					   );
			    fclose(fp);

				/**
					READS THE TRAINING DATA CACHE, FOR PERFORMACE IMPROVEMENT.
				*/
				if (N_ITEMSETS>0 && N_TRANSACTIONS>0) {
					hasCache = 1;

					/*
					printf( "\n N_TRANSACTIONS=%d N_ITEMSETS=%d \n"
						   , N_TRANSACTIONS
						   , N_ITEMSETS
						   );
					*/

					// COUNT_TARGET
					fp = fopen(COUNT_TARGET_FILE, "r");
					while (!feof(fp)) {
						int key, value;
						fscanf(fp, "KEY:%d VALUE:%d\n", &key, &value);
						COUNT_TARGET[key] = value;
					}
				    fclose(fp);

					// TARGET_ID
					fp = fopen(TARGET_ID_FILE, "r");
					while (!feof(fp)) {
						int key, value;
						fscanf(fp, "KEY:%d VALUE:%d\n", &key, &value);
						TARGET_ID[key] = value;
					}
				    fclose(fp);

					// ITEMSETS
					fp = fopen(ITEMSETS_FILE, "r");
					for (int i=0; i<N_ITEMSETS; i++) {

						int KEY, SIZE, COUNT, EVIDENCE, LIST_SIZE, LAYOUT_SIZE;
						fscanf( fp
							   , "KEY:%d SIZE:%d COUNT:%d EVIDENCE:%d LIST_SIZE:%d LAYOUT_SIZE:%d\n"
							   , &KEY
							   , &SIZE
							   , &COUNT
							   , &EVIDENCE
							   , &LIST_SIZE
							   , &LAYOUT_SIZE
							   );

						ITEMSETS[KEY].size = SIZE;
						ITEMSETS[KEY].count = COUNT;
						ITEMSETS[KEY].evidence = EVIDENCE;

						if (LIST_SIZE>0) {
							ITEMSETS[KEY].list=(int*) (malloc(sizeof(int)*LIST_SIZE));
							for (int j=0; j<LIST_SIZE; j++) {
								int LIST_KEY, LIST_VALUE;
								fscanf( fp
									   , "LIST_KEY:%d LIST_VALUE:%d\n"
									   , &LIST_KEY
									   , &LIST_VALUE
									   );
								ITEMSETS[KEY].list[LIST_KEY] = LIST_VALUE;
							}
						}

						if (LAYOUT_SIZE>0) {
							ITEMSETS[KEY].layout=(int*) (malloc(sizeof(int)*LAYOUT_SIZE));
							for (int j=0; j<LAYOUT_SIZE; j++) {
								int LAYOUT_KEY, LAYOUT_VALUE;
								fscanf( fp
									   , "LAYOUT_KEY:%d LAYOUT_VALUE:%d\n"
									   , &LAYOUT_KEY
									   , &LAYOUT_VALUE
									   );
								ITEMSETS[KEY].layout[j] = LAYOUT_VALUE;
							}
						}
					}
				    fclose(fp);

					// CLASS_NAME
					fp = fopen(CLASS_NAME_FILE, "r");
					while (!feof(fp))
					{
						char key[1024];
						int value;
						fscanf(fp, "KEY:%s VALUE:%d\n", &key, &value);
						//printf("CLASS_NAME KEY:%s VALUE:%d\n", key, value);
						CLASS_NAME[key] = value;
					}
				    fclose(fp);

					// SYMBOL_TABLE
					fp = fopen(SYMBOL_TABLE_FILE, "r");
				    while (!feof(fp))
					{
						int key;
						char value[1024];
						fscanf(fp, "KEY:%d VALUE:%s\n", &key, &value);
						//printf("SYMBOL_TABLE KEY:%d VALUE:%s\n", key, value);
						SYMBOL_TABLE[key] = value;
					}
				    fclose(fp);

					// ITEM_MAP
					fp = fopen(ITEM_MAP_FILE, "r");
					while (!feof(fp))
					{
						char key[1024];
						int value;
						fscanf(fp, "KEY:%s VALUE:%d\n", &key, &value);
						//printf("ITEM_MAP KEY:%s VALUE:%d\n", key, value);
						ITEM_MAP[key] = value;
					}
					fclose(fp);

					// TID_CLASSES
					fp = fopen(TID_CLASSES_FILE, "r");
				    while (!feof(fp))
					{
						int key, value;
						fscanf(fp, "KEY:%d VALUE:%d\n", &key, &value);
						//printf("TID_CLASSES KEY:%d VALUE:%d\n", key, value);
						TID_CLASSES[key].insert(value);
					}
				    fclose(fp);

				    printf(" (Working with training cache) ");
				    /*
					printf("COUNT_TARGET[%d] = %d \n", 0, COUNT_TARGET[0]);
					printf("COUNT_TARGET[%d] = %d \n", 1, COUNT_TARGET[1]);
					printf("TARGET_ID[%d] = %d \n", 0, TARGET_ID[0]);
					printf("TARGET_ID[%d] = %d \n", 1, TARGET_ID[1]);
					printf("CLASS_NAME SIZE: %d \n", CLASS_NAME.size());
					printf("SYMBOL_TABLE SIZE: %d \n", SYMBOL_TABLE.size());
					printf("ITEM_MAP SIZE: %d \n", ITEM_MAP.size());
					printf("TID_CLASSES SIZE: %d \n", TID_CLASSES.size());
					*/
					fflush(stdout);
				}
			}
		}
	}

	if (hasCache==0) {

		for(int i=0;i<MAX_CLASSES;i++) {
			sprintf(target_name[i], "CLASS=%d", i);
			CLASS_NAME[target_name[i]]=i;
			COUNT_TARGET[i]=0;
			TARGET_ID[i]=N_ITEMSETS;
			ITEMSETS[N_ITEMSETS].count=0;
			ITEMSETS[N_ITEMSETS].size=1;
			ITEMSETS[N_ITEMSETS].evidence=CLASS;
			ITEMSETS[N_ITEMSETS].layout=(int*)malloc(sizeof(int));
			ITEMSETS[N_ITEMSETS].list=(int*)malloc(sizeof(int));
			ITEMSETS[N_ITEMSETS].layout[0]=N_ITEMSETS;
			ITEMSETS[N_ITEMSETS].list[0]=-1;
			SYMBOL_TABLE[N_ITEMSETS]=strdup(target_name[i]);
			ITEM_MAP[target_name[i]]=N_ITEMSETS;
			++N_ITEMSETS;
		}

		while(1) {
			int attr_type=CLASS;
			char line[200*KB];
			fgets(line, 200*KB, file);
			if(feof(file)) break;
			N_TRANSACTIONS++;
			proc_items.clear();
			target=-1;
			char* item=strtok(line, " \t\n");
			PROC_IDS.insert(item);
			item=strtok(NULL, " \t\n");
			while(item!=NULL) {
				if(CLASS_NAME.find(item)!=CLASS_NAME.end()) {
					target=(int)CLASS_NAME[item];
					COUNT_TARGET[target]++;
					TID_CLASSES[N_TRANSACTIONS].insert(target);
				}
				if(proc_items.find(item)==proc_items.end()) {
					proc_items.insert(item);
					if(ITEM_MAP.find(item)!=ITEM_MAP.end()) {
						int index=(int)(ITEM_MAP[item]);
						ITEMSETS[index].list=(int*) realloc(ITEMSETS[index].list, sizeof(int)*(ITEMSETS[index].count+1));
						ITEMSETS[index].list[ITEMSETS[index].count]=N_TRANSACTIONS;
						ITEMSETS[index].count++;
					}
					else {
						if(item[0]==A1) attr_type=1;
						else if(item[0]==A2) attr_type=2;
						else if(item[0]==A3) attr_type=4;
						else attr_type=CLASS;
						ITEMSETS[N_ITEMSETS].count=1;
						ITEMSETS[N_ITEMSETS].size=1;
						ITEMSETS[N_ITEMSETS].evidence=attr_type;
						ITEMSETS[N_ITEMSETS].layout=(int*)malloc(sizeof(int));
						ITEMSETS[N_ITEMSETS].list=(int*)malloc(sizeof(int));
						ITEMSETS[N_ITEMSETS].layout[0]=N_ITEMSETS;
						ITEMSETS[N_ITEMSETS].list[0]=N_TRANSACTIONS;
						SYMBOL_TABLE[N_ITEMSETS]=strdup(item);
						ITEM_MAP[item]=N_ITEMSETS;
						++N_ITEMSETS;
					}
				}
				item=strtok(NULL, " \t\n");
			}
			//if(N_TRANSACTIONS%(n_lines/(int)100)==0) {
			//	for(unsigned int i=0;i<strlen(done);i++) printf("\b");
			//	sprintf(done,"%d%%",(int)(100*N_TRANSACTIONS/n_lines));
			//	printf("%s", done);
			//	fflush(stdout);
			//}
		}

		/**
			WRITES THE TRAINING DATA CACHE, FOR PERFORMACE IMPROVEMENT.
		*/
		if (CACHE_TRAINING_DATA==1) {

			// VARIAVEIS AUXILIARES
			FILE* fp = fopen(VAR_AUX_FILE, "w");
			fprintf( fp
				   , "N_TRANSACTIONS=%d N_ITEMSETS=%d"
				   , N_TRANSACTIONS
				   , N_ITEMSETS
				   );
		    fclose(fp);

			// COUNT_TARGET
			fp = fopen(COUNT_TARGET_FILE, "w");
			for (int i=0; i<MAX_CLASSES; i++) {
				fprintf(fp, "KEY:%d VALUE:%d\n", i, COUNT_TARGET[i]);
			}
		    fclose(fp);

			// TARGET_ID
			fp = fopen(TARGET_ID_FILE, "w");
			for (int i=0; i<MAX_CLASSES; i++) {
				fprintf(fp, "KEY:%d VALUE:%d\n", i, TARGET_ID[i]);
			}
		    fclose(fp);

		    // ITEMSETS
			fp = fopen(ITEMSETS_FILE, "w");
			for (int i=0; i<N_ITEMSETS; i++) {
				int list_size = ITEMSETS[i].count; // (sizeof(ITEMSETS[i].list)/sizeof(int));
				int layout_size = ITEMSETS[i].size; // sempre sera o tamanho do itemset

				fprintf(fp, "KEY:%d SIZE:%d COUNT:%d EVIDENCE:%d LIST_SIZE:%d LAYOUT_SIZE:%d\n", i, ITEMSETS[i].size, ITEMSETS[i].count, ITEMSETS[i].evidence, list_size, layout_size);

				for (int j=0; j<list_size; j++) {
					if (ITEMSETS[i].list[j]) {
						fprintf(fp, "LIST_KEY:%d LIST_VALUE:%d\n", j, ITEMSETS[i].list[j]);
					}
				}
				for (int j=0; j<layout_size; j++) {
					if (ITEMSETS[i].layout[j]) {
						fprintf(fp, "LAYOUT_KEY:%d LAYOUT_VALUE:%d\n", j, ITEMSETS[i].layout[j]);
					}
				}
			}
		    fclose(fp);

			// TID_CLASSES
			fp = fopen(TID_CLASSES_FILE, "w");
		    for(map<int, set<int> >::iterator itr=TID_CLASSES.begin();itr!=TID_CLASSES.end();itr++) {
				int key = (*itr).first;
				set<int> value = (*itr).second;

				for(set<int>::iterator itr2=value.begin(); itr2!=value.end();itr2++) {
					fprintf(fp, "KEY:%d VALUE:%d\n", key, *itr2);
				}
			}
		    fclose(fp);

			// CLASS_NAME
			fp = fopen(CLASS_NAME_FILE, "w");
		    for(map<string, int>::iterator itr=CLASS_NAME.begin();itr!=CLASS_NAME.end();itr++) {
				fprintf(fp, "KEY:%s VALUE:%d\n", (*itr).first.c_str(), (*itr).second);
			}
		    fclose(fp);

			// SYMBOL_TABLE
			fp = fopen(SYMBOL_TABLE_FILE, "w");
		    for(map<int, string>::iterator itr=SYMBOL_TABLE.begin();itr!=SYMBOL_TABLE.end();itr++) {
				fprintf(fp, "KEY:%d VALUE:%s\n", (*itr).first, (*itr).second.c_str());
			}
		    fclose(fp);

			// ITEM_MAP
			fp = fopen(ITEM_MAP_FILE, "w");
		    for(map<string, int>::iterator itr=ITEM_MAP.begin();itr!=ITEM_MAP.end();itr++) {
				fprintf(fp, "KEY:%s VALUE:%d\n", (*itr).first.c_str(), (*itr).second);
			}
		    fclose(fp);
		}
	}

	//for(unsigned int i=0;i<strlen(done);i++) printf("\b");
	printf("[done]\n");
	for(int i=0;i<MAX_CLASSES;i++) free(target_name[i]);
	free(target_name);
	fclose(file);
	__FINISH_TIMER__

	return(0);
}
