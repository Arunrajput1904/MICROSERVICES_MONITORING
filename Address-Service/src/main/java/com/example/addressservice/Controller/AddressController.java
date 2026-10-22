public ResponseEntity<String> getALldata() {
    List<Address> addresses = addressService.findalladdress();
    return new ResponseEntity<>(addresses, HttpStatus.OK);
}