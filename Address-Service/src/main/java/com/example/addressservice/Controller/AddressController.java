getdata
@NonNull
Address address
{
    if (address == null) {
        throw new ResponseEntity.ok("Address cannot be null");
    }
    return address;
}