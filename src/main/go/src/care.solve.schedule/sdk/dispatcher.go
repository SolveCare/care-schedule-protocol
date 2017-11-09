package sdk

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"

	"fmt"
)

type fn func(shim.ChaincodeStubInterface, []string)pb.Response

type Dispatcher struct {
	mapping map[string]fn
}

func NewDispatcher() Dispatcher {
	dispatcher := Dispatcher{}
	dispatcher.mapping = make(map[string]fn)

	return dispatcher
}

func (d Dispatcher) AddMapping(functionName string, function fn) {
	d.mapping[functionName] = function
}

func (d Dispatcher) Dispatch(stub shim.ChaincodeStubInterface) pb.Response {
	functionName, args := stub.GetFunctionAndParameters()

	functionToInvoke, ok := d.mapping[functionName]
	if !ok {
		return shim.Error(fmt.Sprintf("Unknown function '%v'", functionName))
	}

	return functionToInvoke(stub, args)
}