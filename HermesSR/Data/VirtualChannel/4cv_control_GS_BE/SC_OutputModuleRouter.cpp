#include "SC_OutputModuleRouter.h"

SC_MODULE_EXPORT(outmodulerouter);

void inline outmodulerouter::Timer(){
	++CurrentTime;
}

void inline outmodulerouter::TrafficWatcher(){
	char temp[100];
	FILE* Output[constNumRot][constNumPort][constNumVC];
	unsigned long int cont[constNumRot][constNumPort][constNumVC];
	unsigned long int size[constNumRot][constNumPort][constNumVC];
	unsigned long int currentFlit[constNumRot][constNumPort][constNumVC];
	int rot,port,lane;

	for(rot=0;rot<constNumRot;rot++){
		//roteador n�o � o limite da direita, logo tem a porta EAST
 		if((rot%constNumRotX)!=(constNumRotX-1)){
			for(lane=0;lane<constNumVC;lane++){
				sprintf(temp, "r%dp0l%d.txt", rot,lane);
				Output[rot][0][lane] = fopen(temp, "w");
				cont[rot][0][lane] = 0;
			}
		}
		//roteador n�o � o limite da esquerda, logo tem a porta WEST
 		if((rot%constNumRotX)!=0){
			for(lane=0;lane<constNumVC;lane++){
				sprintf(temp, "r%dp1l%d.txt", rot,lane);
				Output[rot][1][lane] = fopen(temp, "w");
				cont[rot][1][lane] = 0;
			}
		}
		//roteador n�o � o limite superior, logo tem a porta NORTH
 		if((rot/constNumRotX)!=(constNumRotY-1)){
			for(lane=0;lane<constNumVC;lane++){
				sprintf(temp, "r%dp2l%d.txt", rot,lane);
				Output[rot][2][lane] = fopen(temp, "w");
				cont[rot][2][lane] = 0;
			}
		}
		//roteador n�o � o limite inferior, logo tem a porta SOUTH
 		if((rot/constNumRotX)!=0){
			for(lane=0;lane<constNumVC;lane++){
				sprintf(temp, "r%dp3l%d.txt", rot,lane);
				Output[rot][3][lane] = fopen(temp, "w");
				cont[rot][3][lane] = 0;
			}
		}
	}

	while(true){
		for(rot=0;rot<constNumRot;rot++){

			//roteador n�o � o limite da direita, logo tem a porta EAST
			if((rot%constNumRotX)!=(constNumRotX-1)){
				for(lane=0;lane<constNumVC;lane++){
					if(inTx(rot,0) == 1 && inLaneTx(rot,0,lane) == 1 && inCredit(rot,0,lane)==1){
						currentFlit[rot][0][lane] = inData(rot,0);
						fprintf(Output[rot][0][lane], "(%0*X %u)", (int)constFlitSize/4, currentFlit[rot][0][lane], CurrentTime);
						cont[rot][0][lane]++;

						if(cont[rot][0][lane] == 2)
							size[rot][0][lane] = currentFlit[rot][0][lane] + 2;

						if(cont[rot][0][lane]>2 && cont[rot][0][lane]==size[rot][0][lane]){
							fprintf(Output[rot][0][lane], "\n");
							cont[rot][0][lane]=0;
							size[rot][0][lane]=0;
						}
					}
				}
			}
			//roteador n�o � o limite da esquerda, logo tem a porta WEST
			if((rot%constNumRotX)!=0){
				for(lane=0;lane<constNumVC;lane++){
					if(inTx(rot,1) == 1 && inLaneTx(rot,1,lane) == 1 && inCredit(rot,1,lane)==1){
						currentFlit[rot][1][lane] = inData(rot,1);
						fprintf(Output[rot][1][lane], "(%0*X %u)", (int)constFlitSize/4, currentFlit[rot][1][lane], CurrentTime);
						cont[rot][1][lane]++;

						if(cont[rot][1][lane] == 2)
							size[rot][1][lane] = currentFlit[rot][1][lane] + 2;

						if(cont[rot][1][lane]>2 && cont[rot][1][lane]==size[rot][1][lane]){
							fprintf(Output[rot][1][lane], "\n");
							cont[rot][1][lane]=0;
							size[rot][1][lane]=0;
						}
					}
				}
			}
			//roteador n�o � o limite superior, logo tem a porta NORTH
			if((rot/constNumRotX)!=constNumRotY-1){
				for(lane=0;lane<constNumVC;lane++){
					if(inTx(rot,2) == 1 && inLaneTx(rot,2,lane) == 1 && inCredit(rot,2,lane)==1){
						currentFlit[rot][2][lane] = inData(rot,2);
						fprintf(Output[rot][2][lane], "(%0*X %u)", (int)constFlitSize/4, currentFlit[rot][2][lane], CurrentTime);
						cont[rot][2][lane]++;

						if(cont[rot][2][lane] == 2)
							size[rot][2][lane] = currentFlit[rot][2][lane] + 2;

						if(cont[rot][2][lane]>2 && cont[rot][2][lane]==size[rot][2][lane]){
							fprintf(Output[rot][2][lane], "\n");
							cont[rot][2][lane]=0;
							size[rot][2][lane]=0;
						}
					}
				}
			}
			//roteador n�o � o limite inferior, logo tem a porta SOUTH
			if((rot/constNumRotX)!=0){
				for(lane=0;lane<constNumVC;lane++){
					if(inTx(rot,3) == 1 && inLaneTx(rot,3,lane) == 1 && inCredit(rot,3,lane)==1){
						currentFlit[rot][3][lane] = inData(rot,3);
						fprintf(Output[rot][3][lane], "(%0*X %u)", (int)constFlitSize/4, currentFlit[rot][3][lane], CurrentTime);
						cont[rot][3][lane]++;

						if(cont[rot][3][lane] == 2)
							size[rot][3][lane] = currentFlit[rot][3][lane] + 2;

						if(cont[rot][3][lane]>2 && cont[rot][3][lane]==size[rot][3][lane]){
							fprintf(Output[rot][3][lane], "\n");
							cont[rot][3][lane]=0;
							size[rot][3][lane]=0;
						}
					}
				}
			}
		}
		wait();
	}
}
