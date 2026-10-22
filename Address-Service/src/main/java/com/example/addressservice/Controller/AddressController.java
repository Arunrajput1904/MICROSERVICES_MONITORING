public ResponseEntity<Map<String, Object>> getdata(@RequestParam String id) {
    if (id == null || id.isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "ID cannot be null or empty"));
    }
    Address address = addressRepository.findById(id);
    if (address == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Address not found"));
    }
    return ResponseEntity.ok().body(Map.of("address", address));
}