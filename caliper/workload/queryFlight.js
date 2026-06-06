'use strict';

const { WorkloadModuleBase } = require('@hyperledger/caliper-core');

/**
 * Workload ODCZYTU — pobieranie pojedynczego lotu (queryFlight).
 *
 * W fazie przygotowania (initializeWorkloadModule) tworzymy pulę lotów,
 * a potem w teście losowo je odczytujemy.
 */
class QueryFlightWorkload extends WorkloadModuleBase {

    constructor() {
        super();
        this.flightIds = [];
    }

    async initializeWorkloadModule(workerIndex, totalWorkers, roundIndex, roundArguments, sutAdapter, sutContext) {
        await super.initializeWorkloadModule(workerIndex, totalWorkers, roundIndex, roundArguments, sutAdapter, sutContext);

        // Stworz 10 lotow do odczytu
        const seedCount = 10;
        for (let i = 0; i < seedCount; i++) {
            const flightId = `QSEED${this.workerIndex}_${i}`;
            this.flightIds.push(flightId);

            try {
                await this.sutAdapter.sendRequests({
                    contractId: 'airport-cc',
                    contractFunction: 'createFlight',
                    contractArguments: [
                        flightId, 'Seed Air', 'WAW', 'LHR', 'A1',
                        'ON_TIME', '2026-05-29T18:00:00Z'
                    ],
                    invokerIdentity: 'User1',
                    readOnly: false
                });
            } catch (e) {
                // lot moze juz istniec z poprzedniego uruchomienia — OK
            }
        }
    }

    async submitTransaction() {
        // Losowy lot z puli
        const flightId = this.flightIds[Math.floor(Math.random() * this.flightIds.length)];

        const request = {
            contractId: 'airport-cc',
            contractFunction: 'queryFlight',
            contractArguments: [flightId],
            invokerIdentity: 'User1',
            readOnly: true    // to jest ODCZYT (evaluate)
        };

        await this.sutAdapter.sendRequests(request);
    }
}

function createWorkloadModule() {
    return new QueryFlightWorkload();
}

module.exports.createWorkloadModule = createWorkloadModule;
