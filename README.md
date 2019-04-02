# papyrus-patches
This project collect a number of fixes an improvements for Eclipse Papyrus modeling tool.

## Sequence Diagrams
Currently we are focused in providing a feature patch that fixes the current sequence diagrams in Papyrus, and provide a good user experience. 

The patch replace a substantial part of the current implementation but it is kept fully compatible with the current notation model, so the diagrams created with a non-patched version, still can be loaded without problems, always they are not broken. (The current implementation create several inconsistences in the notation and uml models)

### Editing
Editing actions such as move, create, delete elements acts on the concept of blocks. A blocks it is a set of elements that are tight together and they do not have any semantical meaning individually outside of the group. Examples of that are:
- the send event, receive event and the message.
- Synch message, execution specification and reply message.
- etc..

Following this principle, We assume that there is no value in handle them individually, and as such we handle them as atomic elements. So if the send event of a message is moved to another lifeline, the corresponding receive event of the reply message change lifeline accordingly.

The implementation try to enforce that fragments does not overlap. So if a message is created such as one of its event is at the same vertical position of another message, every element in the diagram below that vertical position will be pushed to make place for the new message.
This principle is enforced for all the editing actions.

The implementation, also, introduces a slightly different way to use the diagram editor. We introduce two different edition modes, Nudging and Reordering. 

#### Nudging mode 
When moving or editing one element that represent a fragment or an event, in nudging mode, all the other fragments or events following it will be pushed down or up, following the selected element.

This in practice implies only a visual change in the diagram, the order of fragments in the uml model is not modified at all. And the edition won't be allowed if the result of the action implies a change on the uml model.

#### Reorder mode
When moving or editing one element that represent a fragment or an event, in reorder mode, the element will be removed from the current position and re-inserted in the target position, allowing the uml model and, as such, the interaction fragment order to be modified.

This it will be applicable to messages, send and receive events, start and finish of execution specifications, interaction uses and gates.

### UML fragment order.
The UML specification is quite vague on defining the order of the traces in the interaction, we have taken an assumption in this area:

> When the specification is ambiguous on the order of fragments, we take first the one that appears first in the diagram top to down, left to right.

### Current status

Currently we have fully implemented:
- Lifelines:
- Messages:
    - Async Message
    - Synch messages
    - Create Messages
    - Delete Messages
- Execution Specifications.
- Gates:
  - Formal gates
  - Actual gates 

### Ongoing job:

- Interaction Uses:
  - Some issues when on block operations with messages to its gates.


### Not implemented yet or not fixed yet.
The following things are in the backlog to be implemented as soon as possible:
- Combined Fragments
- Message multiselect ion.
- Copy & paste
- Drag & drop
- Diagram grid alignment (Currently only the default grid with 20 pixels works properly.)
