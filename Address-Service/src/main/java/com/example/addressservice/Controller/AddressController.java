public ResponseEntity<Map<String, Object>> getdata(@PathVariable("id") Long id) {
    Optional<Address> addressOptional = addressRepository.findById(id);
    if (addressOptional.isPresent()) {
        Address address = addressOptional.get();
        Map<String, Object> response = Map.of("id", address.getId(), "name", address.getName(), "details", address.getDetails());
        return ResponseEntity.ok(response);
    } else {
        return ResponseEntity.notFound().build();
    }
}