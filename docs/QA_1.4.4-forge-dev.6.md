# QA — Easy Farmer's Delight 1.4.4-dev.6 (Forge 1.20.1)

## Historical Grafting Support regression pass

1. Place a Grafting Support and install a leaf canopy.
2. Break the canopy with hand, Shears, Silk Touch and Creative mode. Leaves and the stripped-oak graft branch must disappear immediately in every case.
3. In Survival, Shears/Silk Touch recover the installed leaf item; ordinary breaking does not.
4. Leave the empty support loaded for several random ticks. The canopy hitbox must never reappear while Jade reports standby.
5. Reinsert leaves and confirm the canopy renderer + outline/collision reactivate immediately.

## Rootstock targeting/model

1. The four outside stakes/rope frame are decorative and must not intercept targeting.
2. The full central trunk and all visible ground root struts must target/interact with the Grafting Support instead of the block below.
3. Root struts must use vanilla Hanging Roots texture with no blank/missing faces.
4. Cantaloupe, Salmonberry, Fruits Delight Grafting/Orchard and Cutter behavior must remain unchanged.
