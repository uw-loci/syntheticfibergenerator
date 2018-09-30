# Summary
Write a Java application (with GUI) to generate simulated images of collagen and
other biological fibers. Such images could be used to verify the accuracy of
fiber extraction algorithms (CT-FIRE, CurveAlign) or to augment ML training
sets.

## Accuracy Verification
This area will be the initial focus. It will use a top down approach, starting
with bulk fiber properties (based on CT-FIRE output) then drawing a random set
of fibers conforming to those properties.

## ML Training
An avenue to explore in the long term. A generative adversarial network (GAN) or
similar technique could be applied.

# Initial Direction: Accuracy Verification

## Parameters
The full set of CT-FIRE output parameters may overconstrain the image. A mostly
orthogonal subset will need to be chosen for fiber synthesis. Before the image
is drawn the remaining CT-FIRE parameters could be calculated from the fibers.

Parameter ranges should be user-configurable, but known distributions can be
used to set initial values and reasonable limits. For accuracy verification it's
not important that all parameter combinations match something that would
actually be seen in a clinical setting.

## Realistic Images
Because accurately extracting fibers from a perfectly clean image is  trivial,
noise should be applied. Methods and sources could include:

- Poisson noise (from SHG instrumentation)
- Blurring due to imperfect focus
- Downsampling
- Objects obscuring the fibers

In the long term, optical simulations and biological principles could be applied
to improve the realism of images.

## Other Considerations
- Fibers should be generated as a series of points with a width at each point
  (in the form extracted by CT-FIRE). These values should be saved for
  comparison with CT-FIRE results.
- It may be best to generate fibers in a 3D volume and flatten them into an
  image plane.

## Strategy
1. Understand current methods of fiber extraction (CT-FIRE, CurveAlign)
2. Choose parameters for fiber synthesis
3. Write program framework and GUI
4. Use framework and GUI to visualize possible methods of fiber synthesis
5. Incrementally improve fiber synthesis
    - Increase complexity of fiber tracing
    - Noise simulation
    - Optical simulation
    - Biological principles

## Required Resources
- Collagen fiber images
- Current CT-FIRE/CurveAlign code (from LOCI website/GitHub)
- Distributions of fiber parameters
- Code from previous work on image synthesis