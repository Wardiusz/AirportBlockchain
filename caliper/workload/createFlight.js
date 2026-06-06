'use strict';

const { WorkloadModuleBase } = require('@hyperledger/caliper-core');

/**
 * Workload ZAPISU — tworzenie lotow (createFlight).
 * Kazda transakcja ma unikalny flightId (chaincode odrzuca duplikaty).
 */
class CreateFlightWorkload extends WorkloadModuleBase {

    constructor() {
        super();
        this.txIndex = 0;
    }

    async submitTransaction() {
        this.txIndex++;
        // Unikalny ID: worker + indeks + timestamp
        const flightId = `CAL${this.workerIndex}_${this.txIndex}_${Date.now() % 100000}`;

        const request = {
            contractId: 'airport-cc',
            contractFunction: 'createFlight',
            contractArguments: [
                flightId,
                'Caliper Air',
                'WAW',
                'JFK',
                'D1',
                'ON_TIME',
                '2026-05-29T18:00:00Z'
            ],
            invokerIdentity: 'User1',
            readOnly: false   // to jest ZAPIS (submit)
        };

        await this.sutAdapter.sendRequests(request);
    }
}

function createWorkloadModule() {
    return new CreateFlightWorkload();
}

module.exports.createWorkloadModule = createWorkloadModule;
