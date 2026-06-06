'use strict';

const { WorkloadModuleBase } = require('@hyperledger/caliper-core');

/**
 * Workload ODCZYTU — pobieranie wszystkich lotow (queryAllFlights).
 */
class QueryAllFlightsWorkload extends WorkloadModuleBase {

    async submitTransaction() {
        const request = {
            contractId: 'airport-cc',
            contractFunction: 'queryAllFlights',
            contractArguments: [],
            invokerIdentity: 'User1',
            readOnly: true
        };

        await this.sutAdapter.sendRequests(request);
    }
}

function createWorkloadModule() {
    return new QueryAllFlightsWorkload();
}

module.exports.createWorkloadModule = createWorkloadModule;
